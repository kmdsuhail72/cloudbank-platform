#!/usr/bin/env python3
"""Render GitOps manifests and check CloudBank deployment invariants offline."""
from pathlib import Path
import re
import subprocess
import yaml

ROOT = Path(__file__).resolve().parents[1]
EXPECTED = {
    'auth-service', 'api-gateway', 'customer-service', 'account-service',
    'ledger-service', 'notification-service', 'audit-fraud-service',
    'card-service', 'transaction-service', 'beneficiary-service', 'loan-service', 'frontend',
}

def check(condition, message):
    if not condition:
        raise SystemExit(message)

rendered = subprocess.check_output(
    ['kubectl', 'kustomize', str(ROOT / 'deploy/gitops/environments/dev')], text=True,
)
objects = list(yaml.safe_load_all(rendered))
deployments = {o['metadata']['name']: o for o in objects if o['kind'] == 'Deployment'}
services = {o['metadata']['name']: o for o in objects if o['kind'] == 'Service'}
check(set(deployments) == EXPECTED, 'Expected all 12 deployments')
check(set(services) == EXPECTED, 'Expected all 12 services')
tags = set()
for obj in objects:
    check(obj['kind'] != 'Secret', 'Credentials must not be rendered from Git')
    if obj['kind'] != 'Namespace':
        check(obj['metadata']['namespace'] == 'cloudbank', 'Unexpected destination namespace')
for name, deployment in deployments.items():
    pod = deployment['spec']['template']
    container = pod['spec']['containers'][0]
    image = container['image']
    check(image.startswith(f'ghcr.io/kmdsuhail72/cloudbank-platform/{name}:'), f'Wrong image: {image}')
    tag = image.rsplit(':', 1)[1]
    check(tag == 'bootstrap-required' or re.fullmatch(r'sha-[0-9a-f]{40}', tag), 'Use a full commit SHA image tag')
    tags.add(tag)
    check(deployment['spec']['replicas'] == 1, 'Development replicas must be one')
    check(services[name]['spec']['selector'].items() <= pod['metadata']['labels'].items(), f'{name}: service selector mismatch')
    volumes = {v['name']: v for v in pod['spec'].get('volumes', [])}
    if name not in {'frontend', 'notification-service', 'audit-fraud-service'}:
        check(volumes.get('jwt-public', {}).get('secret', {}).get('secretName') == 'cloudbank-jwt-public', f'{name}: public key missing')
    check(('jwt-private' in volumes) == (name == 'auth-service'), 'Private signing key must only be mounted in auth')
check(len(tags) == 1, 'All images must belong to the same release')
ingress = next(o for o in objects if o['kind'] == 'Ingress')
check('nginx.ingress.kubernetes.io/rewrite-target' not in ingress['metadata'].get('annotations', {}), 'Ingress must preserve API paths')
path = ingress['spec']['rules'][0]['http']['paths'][0]
check(path['path'] == '/' and path['backend']['service']['name'] == 'frontend', 'Route requests through the frontend proxy')
app = yaml.safe_load((ROOT / 'deploy/gitops/argocd/application.yaml').read_text())
project = yaml.safe_load((ROOT / 'deploy/gitops/argocd/project.yaml').read_text())
check(app['spec']['source']['repoURL'] in project['spec']['sourceRepos'], 'Project rejects the application repository')
check(app['spec']['destination'] in project['spec']['destinations'], 'Project rejects the application destination')
allowed = {(r['group'], r['kind']) for r in project['spec']['namespaceResourceWhitelist'] + project['spec']['clusterResourceWhitelist']}
for obj in objects:
    group = obj['apiVersion'].split('/')[0] if '/' in obj['apiVersion'] else ''
    check((group, obj['kind']) in allowed, f"Project rejects {obj['kind']}")
print(f'Validated {len(objects)} resources, all 12 workloads, key mounts, routing and Argo CD project permissions.')
if tags == {'bootstrap-required'}:
    print('Bootstrap state: publish images and run promote-gitops.py before applying the Argo CD Application.')
