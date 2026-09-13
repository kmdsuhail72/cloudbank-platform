output "state_bucket_name" {
  description = "Terraform remote-state S3 bucket name."
  value       = aws_s3_bucket.terraform_state.id
}

output "state_bucket_arn" {
  description = "Terraform remote-state S3 bucket ARN."
  value       = aws_s3_bucket.terraform_state.arn
}

output "dev_state_key" {
  description = "Reserved CloudBank development state key."
  value       = "cloudbank/dev/terraform.tfstate"
}

output "native_s3_locking" {
  description = "The development backend must use native S3 lockfiles."
  value       = true
}
