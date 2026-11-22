variable "aws_region" {
  description = "La région AWS"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Nom du cluster EKS"
  type        = string
  default     = "mykubernetes"
}

variable "kubernetes_version" {
  description = "Version de Kubernetes pour EKS"
  type        = string
  default     = "1.33"
}