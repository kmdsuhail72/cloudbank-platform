# CloudBank Development Terraform Environment

This directory is the Terraform root module for the CloudBank development
environment.

## Remote state

- Backend: Amazon S3
- Bucket: `cloudbank-tfstate-494768964999-ap-south-1`
- Key: `cloudbank/dev/terraform.tfstate`
- Region: `ap-south-1`
- Native S3 lockfile: enabled
- DynamoDB locking: not used
- Backend encryption: enabled

AWS credentials must not be stored in Terraform source, tfvars, backend
configuration, Git, or plan files.

Local operators use the standard AWS credential/provider chain. Future CI
authentication will use GitHub Actions OIDC and a least-privilege IAM role.

## Current phase

This scaffold intentionally contains no infrastructure resources yet.

Remote backend initialization and all infrastructure plans/applies remain
separate gated phases.
