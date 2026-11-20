variable "region" {
  description = "La région AWS"
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Le nom du cluster EKS"
  default     = "mykubernetes"
}