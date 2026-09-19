# Kubernetes deployment

These manifests provide a baseline deployment for the CloudBank platform in a Kubernetes cluster.

## Prerequisites

- A Kubernetes cluster
- A compatible ingress controller such as NGINX Ingress
- Container images pushed to a registry available from the cluster
- A PostgreSQL database and Kafka cluster configured for the platform

## Apply the platform

```bash
kubectl apply -f deploy/kubernetes/namespace.yaml
kubectl apply -f deploy/kubernetes/configmap.yaml
kubectl apply -f deploy/kubernetes/secrets.yaml
kubectl apply -f deploy/kubernetes/
```

## Update runtime settings

Before deploying the workloads in a real environment:

- replace the placeholder secret values in `secrets.yaml`
- update image tags in each deployment to match your container registry
- ensure the ingress host (`cloudbank.local`) matches your DNS setup

This baseline is intentionally small and should be extended with autoscaling, ingress TLS, StatefulSets for PostgreSQL and Kafka, and GitOps workflows as the platform grows.
