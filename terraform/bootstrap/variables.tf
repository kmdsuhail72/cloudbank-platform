variable "aws_region" {
  description = "AWS region for the CloudBank Terraform state bucket."
  type        = string
  default     = "ap-south-1"

  validation {
    condition     = var.aws_region == "ap-south-1"
    error_message = "CloudBank bootstrap is currently certified only for ap-south-1."
  }
}

variable "state_bucket_name" {
  description = "Globally unique S3 bucket used for Terraform remote state."
  type        = string
  default     = "cloudbank-tfstate-494768964999-ap-south-1"

  validation {
    condition = (
      length(var.state_bucket_name) >= 3 &&
      length(var.state_bucket_name) <= 63 &&
      can(regex(
        "^[a-z0-9][a-z0-9.-]*[a-z0-9]$",
        var.state_bucket_name
      ))
    )

    error_message = "state_bucket_name must be a valid lowercase S3 bucket name."
  }
}

variable "tags" {
  description = "Tags applied to bootstrap resources."
  type        = map(string)

  default = {
    Project     = "CloudBank"
    ManagedBy   = "Terraform"
    Component   = "TerraformState"
    Environment = "shared"
  }
}
