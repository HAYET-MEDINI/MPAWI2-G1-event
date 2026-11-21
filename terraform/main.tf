# ==========================================
# TERRAFORM CONFIGURATION
# ==========================================

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.67"
    }

    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}

# ==========================================
# AWS PROVIDER
# ==========================================

provider "aws" {
  region = "us-east-1"
}

# ==========================================
# RANDOM PROVIDER
# ==========================================

resource "random_id" "bucket_suffix" {
  byte_length = 4
}

# ==========================================
# S3 BUCKET
# ==========================================

resource "aws_s3_bucket" "event_project_bucket" {
  bucket = "eventsproject-${random_id.bucket_suffix.hex}"

  tags = {
    Name        = "EventsProjectBucket"
    Project     = "EventsProject"
    Environment  = "Dev"
    ManagedBy   = "Terraform"
  }
}
