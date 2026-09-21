#!/usr/bin/env python3
"""Connect single-node MicroK8s to this host's existing CloudBank Docker dependencies.

Run again if Docker recreates containers with new IP addresses. Credentials are
read locally and sent to Kubernetes on stdin; they are never written to Git.

Kafka uses a small host-network proxy because normal Calico pods on this local
MicroK8s installation cannot directly reach the Docker Kafka bridge address.
"""

import argparse
import ipaddress
import json
from pathlib import Path
import subprocess


parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--context', default='microk8s')
args = parser.parse_args()

root = Path(__file__).resolve().parents[1]
kubectl = ['kubectl', '--context', args.context, '--request-timeout=30s']


def apply(obj):
    subprocess.run(
        kubectl
        + [
            'apply',
            '--server-side',
            '--field-manager=cloudbank-local-bootstrap',
            '-f',
            '-',
        ],
        input=json.dumps(obj),
        text=True,
        check=True,
        stdout=subprocess.DEVNULL,
    )


def docker_ip(container):
    info = json.loads(
        subprocess.check_output(['docker', 'inspect', container], text=True)
    )[0]

    if not info['State']['Running']:
        raise SystemExit(f'{container} must be running')

    address = info['NetworkSettings']['Networks']['cloudbank-messaging']['IPAddress']
    ipaddress.IPv4Address(address)
    return address


def node_internal_ip():
    nodes = json.loads(
        subprocess.check_output(
            kubectl + ['get', 'nodes', '-o', 'json'],
            text=True,
        )
    )

    if len(nodes['items']) != 1:
        raise SystemExit(
            'configure-local-cluster.py supports only a single-node local cluster'
        )

    for address in nodes['items'][0]['status']['addresses']:
        if address['type'] == 'InternalIP':
            ipaddress.IPv4Address(address['address'])
            return address['address']

    raise SystemExit('MicroK8s node InternalIP was not found')


def selectorless_service(name, service_port, endpoint_ip, endpoint_port):
    return [
        {
            'apiVersion': 'v1',
            'kind': 'Service',
            'metadata': {
                'name': name,
                'namespace': 'cloudbank',
            },
            'spec': {
                'ports': [
                    {
                        'name': 'tcp',
                        'port': service_port,
                        'targetPort': endpoint_port,
                    }
                ]
            },
        },
        {
            'apiVersion': 'discovery.k8s.io/v1',
            'kind': 'EndpointSlice',
            'metadata': {
                'name': name + '-local',
                'namespace': 'cloudbank',
                'labels': {
                    'kubernetes.io/service-name': name,
                    'endpointslice.kubernetes.io/managed-by':
                        'cloudbank-local-bootstrap',
                },
            },
            'addressType': 'IPv4',
            'ports': [
                {
                    'name': 'tcp',
                    'port': endpoint_port,
                    'protocol': 'TCP',
                }
            ],
            'endpoints': [
                {
                    'addresses': [endpoint_ip],
                    'conditions': {'ready': True},
                }
            ],
        },
    ]


values = {}

for line in (root / '.env.local').read_text().splitlines():
    if line.strip() and not line.startswith('#') and '=' in line:
        key, value = line.split('=', 1)
        values[key] = value.strip().strip('"\'')

credentials = {
    k: v
    for k, v in values.items()
    if k.endswith(('_DB_USER', '_DB_PASSWORD'))
}

for service in ['card', 'transaction', 'beneficiary', 'loan', 'audit']:
    for suffix in ['USER', 'PASSWORD']:
        credentials.setdefault(
            f'CLOUDBANK_{service.upper()}_DB_{suffix}',
            f'cloudbank_{service}',
        )


postgres_ip = docker_ip('cloudbank-postgres')
kafka_ip = docker_ip('cloudbank-kafka')
mailpit_ip = docker_ip('cloudbank-mailpit')
node_ip = node_internal_ip()

proxy_port = 29092


dependencies = []

dependencies.extend(
    selectorless_service(
        'postgres',
        5432,
        postgres_ip,
        5432,
    )
)

dependencies.extend(
    selectorless_service(
        'mailpit',
        1025,
        mailpit_ip,
        1025,
    )
)

# Both Kafka service names use the host-network proxy. The broker itself
# continues advertising cloudbank-kafka:19092 to clients.
dependencies.extend(
    selectorless_service(
        'kafka',
        9092,
        node_ip,
        proxy_port,
    )
)

dependencies.extend(
    selectorless_service(
        'cloudbank-kafka',
        19092,
        node_ip,
        proxy_port,
    )
)


kafka_proxy = {
    'apiVersion': 'apps/v1',
    'kind': 'Deployment',
    'metadata': {
        'name': 'cloudbank-local-kafka-proxy',
        'namespace': 'cloudbank',
        'labels': {
            'app.kubernetes.io/name': 'cloudbank-local-kafka-proxy',
            'app.kubernetes.io/part-of': 'cloudbank-local-bootstrap',
        },
    },
    'spec': {
        'replicas': 1,
        'strategy': {
            'type': 'Recreate',
        },
        'selector': {
            'matchLabels': {
                'app.kubernetes.io/name': 'cloudbank-local-kafka-proxy',
            }
        },
        'template': {
            'metadata': {
                'labels': {
                    'app.kubernetes.io/name': 'cloudbank-local-kafka-proxy',
                    'app.kubernetes.io/part-of': 'cloudbank-local-bootstrap',
                }
            },
            'spec': {
                'hostNetwork': True,
                'dnsPolicy': 'ClusterFirstWithHostNet',
                'terminationGracePeriodSeconds': 2,
                'containers': [
                    {
                        'name': 'proxy',
                        'image': 'busybox:1.36',
                        'imagePullPolicy': 'IfNotPresent',
                        'command': [
                            'sh',
                            '-c',
                            (
                                'echo "Kafka proxy listening on :29092"; '
                                f'exec nc -ll -p {proxy_port} '
                                f'-e nc {kafka_ip} 19092'
                            ),
                        ],
                        'ports': [
                            {
                                'name': 'kafka-proxy',
                                'containerPort': proxy_port,
                                'protocol': 'TCP',
                            }
                        ],
                    }
                ],
            },
        },
    },
}


apply(
    {
        'apiVersion': 'v1',
        'kind': 'Namespace',
        'metadata': {'name': 'cloudbank'},
    }
)

apply(
    {
        'apiVersion': 'v1',
        'kind': 'Secret',
        'metadata': {
            'name': 'cloudbank-secrets',
            'namespace': 'cloudbank',
        },
        'type': 'Opaque',
        'stringData': credentials,
    }
)

for kind in ['public', 'private']:
    name = f'jwt-{kind}.pem'

    apply(
        {
            'apiVersion': 'v1',
            'kind': 'Secret',
            'metadata': {
                'name': f'cloudbank-jwt-{kind}',
                'namespace': 'cloudbank',
            },
            'type': 'Opaque',
            'stringData': {
                name: (
                    root
                    / 'services/auth-service/keys'
                    / name
                ).read_text()
            },
        }
    )


apply(kafka_proxy)

for obj in dependencies:
    apply(obj)


print(
    'Configured runtime Secrets, PostgreSQL/Mailpit endpoints, '
    'and the local Kafka host-network proxy.'
)
