# Kubernetes deployment

These manifests provide a baseline deployment for the CloudBank platform in a Kubernetes cluster.

## Prerequisites

- A Kubernetes cluster
- A compatible ingress controller such as NGINX Ingress
- Container images pushed to a registry available from the cluster
- A PostgreSQL database and Kafka cluster configured for the platform

## Deploy with GitOps

Use [the Argo CD GitOps guide](../gitops/README.md) for the development overlay,
container publishing, runtime prerequisites and release promotion. The overlay
adds database configuration, JWT mounts and frontend ingress routing to this base.

## Render the base

```bash
kubectl kustomize deploy/kubernetes/
```

## Update runtime settings

Before deploying the workloads in a real environment:

- create the runtime and JWT Secrets outside Git, as described in the GitOps guide
- update image tags in each deployment to match your container registry
- ensure the ingress host (`cloudbank.local`) matches your DNS setup

This base does not provision PostgreSQL, Kafka, or an ingress controller. Use the
development overlay for deployment; add autoscaling and ingress TLS as needed.
