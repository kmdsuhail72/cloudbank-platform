variable "name_prefix" {
  description = "Name prefix applied to CloudBank network resources."
  type        = string

  validation {
    condition     = length(trimspace(var.name_prefix)) > 0
    error_message = "name_prefix must not be empty."
  }
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string

  validation {
    condition     = can(cidrnetmask(var.vpc_cidr))
    error_message = "vpc_cidr must be a valid IPv4 CIDR."
  }
}

variable "availability_zones" {
  description = "Exactly three availability zones used by the network."
  type        = list(string)

  validation {
    condition = (
      length(var.availability_zones) == 3 &&
      length(distinct(var.availability_zones)) == 3
    )
    error_message = "Exactly three distinct availability zones are required."
  }
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs keyed by availability zone."
  type        = map(string)

  validation {
    condition = (
      length(var.public_subnet_cidrs) == 3 &&
      alltrue([
        for az in var.availability_zones :
        contains(keys(var.public_subnet_cidrs), az)
      ]) &&
      alltrue([
        for cidr in values(var.public_subnet_cidrs) :
        can(cidrnetmask(cidr))
      ])
    )
    error_message = "Public subnet CIDRs must contain exactly the three configured availability zones."
  }
}

variable "private_app_subnet_cidrs" {
  description = "Private application subnet CIDRs keyed by availability zone."
  type        = map(string)

  validation {
    condition = (
      length(var.private_app_subnet_cidrs) == 3 &&
      alltrue([
        for az in var.availability_zones :
        contains(keys(var.private_app_subnet_cidrs), az)
      ]) &&
      alltrue([
        for cidr in values(var.private_app_subnet_cidrs) :
        can(cidrnetmask(cidr))
      ])
    )
    error_message = "Private application CIDRs must contain exactly the three configured availability zones."
  }
}

variable "private_data_subnet_cidrs" {
  description = "Private data subnet CIDRs keyed by availability zone."
  type        = map(string)

  validation {
    condition = (
      length(var.private_data_subnet_cidrs) == 3 &&
      alltrue([
        for az in var.availability_zones :
        contains(keys(var.private_data_subnet_cidrs), az)
      ]) &&
      alltrue([
        for cidr in values(var.private_data_subnet_cidrs) :
        can(cidrnetmask(cidr))
      ])
    )
    error_message = "Private data CIDRs must contain exactly the three configured availability zones."
  }
}

variable "tags" {
  description = "Tags applied to network resources."
  type        = map(string)
  default     = {}
}
