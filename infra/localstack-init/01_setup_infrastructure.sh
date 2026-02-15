#!/bin/bash
echo "Setting up S3 and SQS ..."

# Create s3 bucket and queue
awslocal s3 mb s3://raw-video-bucket
awslocal s3 mb s3://streams-bucket
awslocal sqs create-queue --queue-name processing-queue

# Configure CORS for raw-video-bucket (for multipart uploads from browser)
awslocal s3api put-bucket-cors --bucket raw-video-bucket --cors-configuration '{
  "CORSRules": [
    {
      "AllowedOrigins": ["*"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
      "AllowedHeaders": ["*"],
      "ExposeHeaders": ["ETag", "Content-Length", "Content-Range"],
      "MaxAgeSeconds": 3600
    }
  ]
}'

# Configure CORS for streams-bucket (for video playback from browser)
awslocal s3api put-bucket-cors --bucket streams-bucket --cors-configuration '{
  "CORSRules": [
    {
      "AllowedOrigins": ["*"],
      "AllowedMethods": ["GET", "HEAD"],
      "AllowedHeaders": ["*"],
      "ExposeHeaders": ["Content-Length", "Content-Range"],
      "MaxAgeSeconds": 3600
    }
  ]
}'

echo "CORS configured for S3 buckets ..."

# Get Queue ARN
QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/processing-queue \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

# Configure s3 to push notification on SQS whenever object is created
awslocal s3api put-bucket-notification-configuration \
  --bucket raw-video-bucket \
  --notification-configuration '{
    "QueueConfigurations": [
      {
        "QueueArn": "'"$QUEUE_ARN"'",
        "Events": ["s3:ObjectCreated:*"]
      }
    ]
  }'

echo "Finished setting up Raw Video Bucket Notifications ..."

# Create CloudFront CDN for distribution
# DISTRIBUTION_ID=$(awslocal cloudfront create-distribution \
#   --origin-domain-name streams-bucket.s3.localhost.localstack.cloud:4566 \
#   --default-root-object index.html \
#   --query 'Distribution.Id' \
#   --output text)

# Temporary dummy ID, just to simulate a CDN
DISTRIBUTION_ID="E3ABCDEFG"

echo "CloudFront Distribution Created: $DISTRIBUTION_ID"

# Store this ID in SSM Parameter Store so Play Service can use it
awslocal ssm put-parameter \
  --name "/app/cloudfront-distribution-id" \
  --value "$DISTRIBUTION_ID" \
  --type String \
  --overwrite