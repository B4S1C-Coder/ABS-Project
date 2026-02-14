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

## Idempotent Job Processing
Since there can be multiple instances of the `video-processing-service` (aka `chunk-service`), it is important to ensure that video is only **processed once** and by a **single worker** (at any given time). Along with this, we would also want to ensure that in case of a failure, the job is not left hanging (i.e. a situation in which a video is not processed by any worker at all).

Each `video-processing-service` would *"lease"* i.e. temporarily own the job (video to process). It works by updating heartbeat timestamps in the `metadata-db` to assert it's ownership throughout the duration of processing. Whenever a heartbeat timestamp is not updated within an acceptible timeframe, then that job can be claimed by any other instance.

Detailed working below:
<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./ref/JobLeaseDark.png">
  <source media="(prefers-color-scheme: light)" srcset="./ref/JobLeaseLight.png">
  <img alt="System Architecture Diagram" src="./ref/JobLeaseDark.png" width="600">
</picture>

## Deployment
![aws-arch](./deploy/deployed_arc.png)

More details in [deploy/README.md](./deploy/README.md).

## Running Locally
### Development Setup
>**Note**: Observability tools: Elasticsearch, Kibana, Logstash will not be started during development. If you do need them then use ```docker compose -f ./infra/dev/docker-compose.yml --profile observability up```

For the development setup use [`./infra/dev/docker-compose.yml`](./infra/dev/docker-compose.yml).
1. First run the docker compose to get the required infrastructure up and running:
```bash
docker compose -f ./infra/dev/docker-compose.yml up
```

2. Build everything (especially the first time and whenever changes are made in `common`)
```bash
./mvnw clean install -DskipTests
```

3. Ensure you run `upload-service` before the `video-processing-service` since upload-service owns the schemas shared between the two. The `play-service` might be run in any order.
```bash
./mvnw spring-boot:run -pl upload-service
```

```bash
./mvnw spring-boot:run -pl video-processing-service
```

```bash
./mvnw spring-boot:run -pl play-service
```

### Running Locally
The entire `Project Stack` -> `LocalStack(S3, SQS)` + `NGINX` + `Postgres` + `ElasticSearch` + `Kibana` + `Logstash` + `Kafka` + `upload-service` + `video-processing-service` + `play-service` can take upwards of **7 GB RAM**. During testing with a particularly large video, WSL2 ran out of memory and crashed. As a result all the volumes of ELK Stack were left in an unhealthy state and had to be scraped.

So, do ensure you increase the memory limit in your `.wslconfig` if you plan to run the full project.

#### First Time Setup
Build the docker images locally:
```bash
docker build -f upload-service/Dockerfile -t sp-upload-service:v3.0 .
docker build -f play-service/Dockerfile -t sp-play-service:v3.0 .
docker build -f video-processing-service/Dockerfile -t sp-video-processing-service:v3.0 .
```
#### Running
For running the full project with observability:
```bash
docker compose -f ./infra/local/docker-compose.yml --profile observability up
```

For running the project without observability:
```bash
docker compose -f ./infra/dev/docker-compose.yml up
```
