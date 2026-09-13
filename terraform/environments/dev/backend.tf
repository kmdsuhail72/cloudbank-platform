terraform {
  backend "s3" {
    bucket       = "cloudbank-tfstate-494768964999-ap-south-1"
    key          = "cloudbank/dev/terraform.tfstate"
    region       = "ap-south-1"
    use_lockfile = true
    encrypt      = true
  }
}
