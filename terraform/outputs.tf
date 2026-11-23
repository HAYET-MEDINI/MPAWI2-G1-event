
output "cluster_endpoint" {
  description = "L'endpoint du cluster EKS"
  value       = aws_eks_cluster.my_cluster.endpoint
}

output "cluster_name" {
  description = "Nom du cluster EKS"
  value       = aws_eks_cluster.my_cluster.name
}

output "cluster_role_arn" {
  description = "L'ARN du rôle IAM du cluster EKS"
  value       = aws_eks_cluster.my_cluster.role_arn
}