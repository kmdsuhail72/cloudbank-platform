# CloudBank GitOps

Argo CD reconciles `deploy/gitops/environments/dev` from the `main` branch of
`https://github.com/kmdsuhail72/cloudbank-platform.git` into the `cloudbank`
namespace. The overlay includes the frontend and all eleven backend services.
It uses one replica per workload, resource budgets, runtime database addresses,
and JWT Secret mounts. The ingress sends requests through the frontend's Nginx
proxy, preserving `/api` paths.

## Release flow

1. Merge application changes into `main`, or run **Publish images** from GitHub
   Actions. This builds twelve images and publishes them to
   `ghcr.io/kmdsuhail72/cloudbank-platform/<service>:sha-<full-commit>`.
2. Wait for the entire workflow to succeed. Then run:

   ```bash
   python3 scripts/promote-gitops.py <full-40-character-successfully-built-commit>
   python3 scripts/validate-gitops.py
   ```

3. Review and commit the overlay change through your normal PR process. When it
   reaches `main`, Argo CD deploys the selected release. CI does not need cluster
   credentials. A manifest-only promotion does not trigger another image build.

The initial `bootstrap-required` tags deliberately do not name published images.
Publish and promote the first release before creating the Application. The
promotion helper validates the SHA format; it does not query registry availability.
Only promote a fully successful workflow. GHCR tags can be overwritten by a
publisher; restrict package write permissions and do not reuse release tags.

## Cluster prerequisites

Provide a Kubernetes cluster, Argo CD, an NGINX ingress controller, and the
following dependencies before enabling reconciliation:

- PostgreSQL reachable as `postgres.cloudbank.svc.cluster.local:5432`, with
  databases `cloudbank_core`, `cloudbank_customer`, `cloudbank_account`,
  `cloudbank_ledger`, `cloudbank_notification`, `cloudbank_card`,
  `cloudbank_transaction`, `cloudbank_beneficiary`, `cloudbank_loan`, and
  `cloudbank_audit`. Configure owners and passwords to match the runtime Secret.
- Kafka reachable as `kafka:9092`, with `cloudbank.user-contact.v1` and
  `cloudbank.transfer.v1` created. Mailpit should be reachable as
  `mailpit.cloudbank.svc.cluster.local:1025` for development email delivery.
- A `cloudbank-secrets` Secret in the `cloudbank` namespace containing
  `CLOUDBANK_DB_USER` and `CLOUDBANK_DB_PASSWORD`, plus
  `CLOUDBANK_<SERVICE>_DB_USER` and `CLOUDBANK_<SERVICE>_DB_PASSWORD` for CUSTOMER,
  ACCOUNT, LEDGER, NOTIFICATION, CARD, TRANSACTION, BENEFICIARY, LOAN and AUDIT.
  Audit uses the `cloudbank_audit` database role explicitly in its deployment.

Adjust `runtime-config.yaml` and the audit Deployment if your infrastructure uses
different addresses, database names or roles. The local Docker infrastructure
started outside Kubernetes is not automatically reachable at these cluster DNS names.

Create the namespace and Secrets using local files outside Git:

```bash
kubectl apply -f deploy/kubernetes/namespace.yaml
kubectl -n cloudbank create secret generic cloudbank-secrets \
  --from-env-file=/secure/path/cloudbank-kubernetes.env
kubectl -n cloudbank create secret generic cloudbank-jwt-public \
  --from-file=jwt-public.pem=services/auth-service/keys/jwt-public.pem
kubectl -n cloudbank create secret generic cloudbank-jwt-private \
  --from-file=jwt-private.pem=services/auth-service/keys/jwt-private.pem
```

The public key is mounted into JWT consumers; the private key is mounted only into
auth-service. Secrets are intentionally excluded from Kustomize and Argo CD's
resource allowlist. Use your existing secret-management system for ongoing rotation.

GHCR packages must be public or accessible through an image pull Secret. For
private images, create `ghcr-pull` from a local Docker credential file and attach it
to the namespace's default service account before rollout:

```bash
kubectl -n cloudbank create secret generic ghcr-pull \
  --type=kubernetes.io/dockerconfigjson \
  --from-file=.dockerconfigjson=/secure/path/config.json
kubectl -n cloudbank patch serviceaccount default \
  -p '{"imagePullSecrets":[{"name":"ghcr-pull"}]}'
```

## Bootstrap Argo CD

If Argo CD is not installed, select an approved release tag from the
[official installation guide](https://argo-cd.readthedocs.io/en/stable/getting_started/).
Set `ARGOCD_VERSION` to that exact tag, then install into the intended context:

```bash
kubectl config current-context
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -n argocd --server-side -f \
  "https://raw.githubusercontent.com/argoproj/argo-cd/${ARGOCD_VERSION:?Set an exact release tag}/manifests/install.yaml"
kubectl -n argocd rollout status deployment/argocd-server --timeout=300s
```

For a private Git repository, configure Argo CD repository credentials before
creating the Application. See the [private repository documentation](https://argo-cd.readthedocs.io/en/stable/user-guide/private-repositories/).

After the first release is published, its overlay is merged, and prerequisites
exist, register the project and Application:

```bash
kubectl apply -f deploy/gitops/argocd/project.yaml
kubectl apply -f deploy/gitops/argocd/application.yaml
kubectl -n argocd get application cloudbank-dev
kubectl -n argocd port-forward svc/argocd-server 8443:443
```

Open `https://localhost:8443`. Username is `admin`; obtain the initial password with:

```bash
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath='{.data.password}' | base64 --decode
```

Argo CD enables automated sync, drift correction and pruning of resources it
manages. The Application has no cascade-deletion finalizer. Removing Git-managed
resources from the overlay can delete them on the next sync. The project's
permissions cover only the workload kinds and namespace required here.

## Verify and roll back

```bash
python3 scripts/validate-gitops.py
kubectl -n argocd get application cloudbank-dev
kubectl -n cloudbank get deployments,pods,services,ingress
kubectl -n cloudbank port-forward svc/frontend 8080:80
```

Use an unused local port if the standalone app is still running on 8080. For
ingress access, point `cloudbank.local` at the ingress controller's address.
Expect the Application to become `Synced` and `Healthy` after dependencies and
images are available. To roll back, revert the promotion commit or promote a
previous successfully published SHA, then merge the overlay change. Do not rely
on `kubectl rollout undo`: automated sync restores the Git version.

The validation workflow renders manifests and checks workload coverage, image
tags, routing, key isolation and Argo CD permissions. It does not replace a live
cluster rollout check. PostgreSQL, Kafka, Argo CD itself and the standalone
Prometheus/Grafana installation are not managed by this Application.

References: [Argo CD automated sync](https://argo-cd.readthedocs.io/en/stable/user-guide/auto_sync/),
[Kustomize](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/).
