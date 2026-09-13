variable "aws_region" {
  description = "AWS region for the CloudBank development environment."
  type        = string
  default     = "ap-south-1"

  validation {
    condition     = var.aws_region == "ap-south-1"
    error_message = "CloudBank dev is currently certified only for ap-south-1."
  }
}

variable "tags" {
  description = "Default tags for CloudBank development resources."
  type        = map(string)

  default = {
    Project     = "CloudBank"
    ManagedBy   = "Terraform"
    Environment = "dev"
  }
}
