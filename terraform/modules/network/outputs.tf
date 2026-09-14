output "vpc_id" {
  description = "CloudBank VPC ID."
  value       = aws_vpc.this.id
}

output "public_subnet_ids" {
  description = "Public subnet IDs keyed by availability zone."
  value = {
    for az, subnet in aws_subnet.public :
    az => subnet.id
  }
}

output "private_app_subnet_ids" {
  description = "Private application subnet IDs keyed by availability zone."
  value = {
    for az, subnet in aws_subnet.private_app :
    az => subnet.id
  }
}

output "private_data_subnet_ids" {
  description = "Private data subnet IDs keyed by availability zone."
  value = {
    for az, subnet in aws_subnet.private_data :
    az => subnet.id
  }
}

output "nat_gateway_id" {
  description = "Development NAT Gateway ID."
  value       = aws_nat_gateway.this.id
}

output "public_route_table_id" {
  description = "Public route table ID."
  value       = aws_route_table.public.id
}

output "private_app_route_table_id" {
  description = "Private application route table ID."
  value       = aws_route_table.private_app.id
}

output "private_data_route_table_id" {
  description = "Private data route table ID."
  value       = aws_route_table.private_data.id
}
