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

For Argo CD deployment, image publishing and release promotion, see
[the GitOps guide](deploy/gitops/README.md).

A baseline Kubernetes manifest set is available in [deploy/kubernetes/](deploy/kubernetes/). It includes a namespace, shared ConfigMap and Secret, the core service Deployments and Services, and a sample ingress definition for routing traffic to the API gateway and frontend.

Use the manifests as a starting point for EKS or any Kubernetes cluster:

```bash
kubectl apply -f deploy/kubernetes/
```

Before production use, replace the placeholder secret values and push the container images to a registry reachable by the cluster.

## Card Service

The `card-service` manages customer payment cards and card lifecycle state. It
stores card metadata in PostgreSQL and exposes authenticated endpoints for
listing cards, creating new cards, retrieving a specific card, and updating card
status.

- HTTP/health port: `8088`
- Database: `cloudbank_card`
- Typical routes: `/api/v1/cards`, `/api/v1/cards/{cardId}`, and
  `/api/v1/cards/{cardId}/status`

The local Compose deployment includes the service in
[deploy/application/compose.yml](./deploy/application/compose.yml), and its
Kubernetes Deployment and Service are in
[deploy/kubernetes/card-service.yaml](./deploy/kubernetes/card-service.yaml).

## Transaction Service

The `transaction-service` stores customer transaction history and exposes
listing, retrieval, creation, and status-update endpoints over authenticated
API routes.

- HTTP/health port: `8089`
- Database: `cloudbank_transaction`
- Typical routes: `/api/v1/transactions`, `/api/v1/transactions/account/{accountId}`,
  `/api/v1/transactions/{transactionId}`, and `/api/v1/transactions/{transactionId}/status`

The local Compose deployment includes the service in
[deploy/application/compose.yml](./deploy/application/compose.yml), and its
Kubernetes Deployment and Service are in
[deploy/kubernetes/transaction-service.yaml](./deploy/kubernetes/transaction-service.yaml).

## Beneficiary Service

The `beneficiary-service` manages saved payees that customers can use for
transfers and recurring payments.

- HTTP/health port: `8090`
- Database: `cloudbank_beneficiary`
- Typical routes: `/api/v1/beneficiaries`, `/api/v1/beneficiaries/{beneficiaryId}`

The local Compose deployment includes the service in
[deploy/application/compose.yml](./deploy/application/compose.yml), and its
Kubernetes Deployment and Service are in
[deploy/kubernetes/beneficiary-service.yaml](./deploy/kubernetes/beneficiary-service.yaml).

## Loan Service

The `loan-service` manages customer loan applications and lifecycle updates.

- HTTP/health port: `8091`
- Database: `cloudbank_loan`
- Typical routes: `/api/v1/loans`, `/api/v1/loans/{loanId}`, and
  `/api/v1/loans/{loanId}/status`

The local Compose deployment includes the service in
[deploy/application/compose.yml](./deploy/application/compose.yml), and its
Kubernetes Deployment and Service are in
[deploy/kubernetes/loan-service.yaml](./deploy/kubernetes/loan-service.yaml).

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

## Run the banking features with Docker

The local application now includes cards, saved beneficiaries, loan applications,
manual transaction records, and Kafka audit ingestion, plus an Nginx-served React
frontend. Docker builds compile the Java and TypeScript sources.

Use the existing local infrastructure: `cloudbank-postgres` (with the core service
schemas), Kafka and Mailpit on `cloudbank-messaging`, root `.env.local`, and the RSA
keys under `services/auth-service/keys`. The feature database initializer is
idempotent and leaves existing roles and databases untouched. Its development
credentials match `deploy/application/compose.yml`.

```bash
docker start cloudbank-postgres
bash deploy/application/init-feature-databases.sh
# Build individually to keep memory use down on development machines.
for service in card-service transaction-service beneficiary-service loan-service audit-fraud-service api-gateway frontend; do
  docker compose -f deploy/application/compose.yml build "$service" || break
done
docker compose -f deploy/application/compose.yml up -d --wait
```

Open **http://localhost:8080**. The frontend proxies `/api` to the gateway and
supports refreshing nested routes. The Vite development server remains available
with `npm run dev --prefix frontend`.

The four new customer services use Flyway migrations and validate their database
schemas at startup. Card, loan, and transaction status updates accept a JSON
object such as `{"status":"BLOCKED"}`. Manual transaction records are separate
from ledger postings and do not move money; account transaction history continues
to come from the ledger. Filter manual records with
`/api/v1/transactions/account/{accountId}`. Records must reference an account owned
by the authenticated customer and use its currency.

Loan applications start in `APPLICATION`; customers can withdraw a pending
application (`REJECTED`). Approval and disbursement require a future staff workflow.
Cards currently represent simulated card metadata, not network-issued cards.
The audit service persists contact and transfer events with duplicate-event
protection; automated fraud scoring is not implemented.

Run the local integration checks (these create isolated test customers and records):

```bash
python3 scripts/smoke-banking.py
python3 scripts/smoke-audit.py
```
