# Streaming Platform
Project to learn about Adaptive Bit Rate Streaming.

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./SysDesignDark.png">
  <source media="(prefers-color-scheme: light)" srcset="./SysDesignLight.png">
  <img alt="System Architecture Diagram" src="./SysDesignDark.png" width="600">
</picture>

## Architecture
The goal of this project is to learn about the video processing pipelines used in streaming platforms such as Netflix and Prime Video. The project uses a multi-service architecture:
- `Upload Service`: Used to upload the videos to s3 (multipart upload)
- `Video Processing Service`: Pulls raw video from s3, converts into HLS 1080p, 720p, 480p, 240p for `Adaptive Bitrate Streaming`, also utilizes GPU Acceleration for this process (`Nvidia RTX 3050 4GB Laptop GPU`)
- Play API: Client facing "dumb" API that just gives the available (`.m3u8`) and handles auth, basically the entrypoint.

>**Note**: PlayAPI is work in progress, but the core feature i.e. the video processing pipeline is complete.

## Techstack
Spring Boot 4.0.1, Postgres, AWS S3, AWS SQS (AWS Services provided via Localstack).

Benchmarks coming soon...