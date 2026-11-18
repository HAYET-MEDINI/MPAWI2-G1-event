
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

variable "cluster_role_arn" {
  description = "ARN du rôle IAM du cluster EKS"
  type        = string
  default     = "arn:aws:iam::279406887041:role/c231366a5819305l18507056t1w279406-LabEksClusterRole-TwlCDNWkcZ1I"
}

variable "node_role_arn" {
  description = "ARN du rôle IAM des noeuds EKS"
  type        = string
  default     = "arn:aws:iam::279406887041:role/c231366a5819305l18507056t1w279406887-LabEksNodeRole-cQeudQgECA6j"
}

variable "subnet_ids" {
  description = "IDs des sous-réseaux"
  type        = list(string)
  default     = [
    "subnet-0cddd7f88ffc78750",
    "subnet-02964a13c4165e0bb"
  ]
}


