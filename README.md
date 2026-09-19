# CloudBank Platform

CloudBank is a cloud-native digital banking platform built from scratch using Java Spring Boot, React, Docker, Kubernetes, GitHub Actions, GitOps, security scanning, and observability.

## Planned Technology Stack

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- Apache Kafka
- gRPC / Protocol Buffers

### Frontend
- React
- TypeScript
- Vite

### DevOps
- Docker
- Docker Compose
- Kubernetes
- Helm
- GitHub Actions
- GitHub Container Registry
- Argo CD
- Terraform
- Amazon EKS

### DevSecOps
- Trivy
- Checkov

### Observability
- Prometheus
- Grafana
- Elasticsearch
- Logstash
- Kibana
- Filebeat

## Architecture

The platform will contain 11 backend services:

1. API Gateway
2. Auth Service
3. Customer Service
4. Account Service
5. Ledger Service
6. Transaction Service
7. Beneficiary Service
8. Card Service
9. Loan Service
10. Notification Service
11. Audit/Fraud Service

## Status

Project initialization in progress.

## Kubernetes deployment

A baseline Kubernetes manifest set is available in [deploy/kubernetes/](deploy/kubernetes/). It includes a namespace, shared ConfigMap and Secret, the core service Deployments and Services, and a sample ingress definition for routing traffic to the API gateway and frontend.

Use the manifests as a starting point for EKS or any Kubernetes cluster:

```bash
kubectl apply -f deploy/kubernetes/
```

Before production use, replace the placeholder secret values and push the container images to a registry reachable by the cluster.

## Audit/Fraud Service

The `audit-fraud-service` consumes CloudBank event envelopes from Kafka and stores
an immutable audit trail in PostgreSQL. It is intentionally a foundation service:
fraud rules and risk scoring can be added on top of the persisted event stream
without coupling those decisions to transaction processing.

- HTTP/health port: `8087`
- Database: `cloudbank_audit`
- Kafka topics: `cloudbank.user-contact.v1` and `cloudbank.transfer.v1`
- Consumer group: `cloudbank-audit-fraud-v1`

The local Compose deployment includes the service in
[deploy/application/compose.yml](./deploy/application/compose.yml), and its
Kubernetes Deployment and Service are in
[deploy/kubernetes/audit-fraud-service.yaml](./deploy/kubernetes/audit-fraud-service.yaml).
