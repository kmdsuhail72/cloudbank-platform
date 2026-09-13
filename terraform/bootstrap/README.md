# CloudBank Terraform State Bootstrap

This stack creates the S3 bucket used for CloudBank Terraform remote state.

## Architecture

- Region: `ap-south-1`
- Bucket: `cloudbank-tfstate-494768964999-ap-south-1`
- Versioning: enabled
- Server-side encryption: AES256
- S3 Block Public Access: fully enabled
- Object ownership: BucketOwnerEnforced
- TLS-only bucket policy
- `prevent_destroy = true`
- `force_destroy = false`
- No DynamoDB lock table

The bootstrap stack intentionally uses local Terraform state because the
remote state bucket does not exist before bootstrap.

The later CloudBank development stack will use:

- S3 key: `cloudbank/dev/terraform.tfstate`
- Native S3 locking: `use_lockfile = true`

## Security

Do not place AWS access keys, passwords, tokens, private keys, or other
credentials in Terraform source, tfvars, backend configuration, or Git.

Future CI authentication must use GitHub Actions OIDC.

## Execution guard

`terraform apply` is prohibited until the CloudBank workflow reaches an
explicit AWS mutation/cost approval gate.

This bootstrap phase is source-only.
