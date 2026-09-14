module "network" {
  source = "../../modules/network"

  name_prefix = "cloudbank-dev"
  vpc_cidr    = "10.40.0.0/16"

  availability_zones = [
    "ap-south-1a",
    "ap-south-1b",
    "ap-south-1c",
  ]

  public_subnet_cidrs = {
    "ap-south-1a" = "10.40.0.0/20"
    "ap-south-1b" = "10.40.16.0/20"
    "ap-south-1c" = "10.40.32.0/20"
  }

  private_app_subnet_cidrs = {
    "ap-south-1a" = "10.40.64.0/20"
    "ap-south-1b" = "10.40.80.0/20"
    "ap-south-1c" = "10.40.96.0/20"
  }

  private_data_subnet_cidrs = {
    "ap-south-1a" = "10.40.128.0/20"
    "ap-south-1b" = "10.40.144.0/20"
    "ap-south-1c" = "10.40.160.0/20"
  }

  tags = var.tags
}
