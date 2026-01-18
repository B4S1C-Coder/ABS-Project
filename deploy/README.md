# Deployment Artifacts
This folder contains the deployment artifacts for the project. Since at the time of making this project, I am just a broke college student, everything must be deployed in `AWS Free tier` without exhausting it. As such only `upload-service` and  `play-service` would be deployed on `AWS EC2` and `video-processing-service` will be run locally.

## Current deployment plan
- `EC2 Instance A`: Will contain an NGINX server, which acts as a gateway and a CDN (`CloudFront` can exhaust my free tier, so this is a workaround), `/etc/hosts` will act as the DNS (`Route53` is not in Free Tier). This instance will e public facing.

- `EC2 Instance B`: Will contain both the `upload-service` and `play-service`, only accessible by `EC2 Instance A`.

- `Private Network`: Consists of `AWS S3`, `AWS SQS`, `AWS EC2 Instance B`.

- `External Private Network`: Consists of `Postgres via (Neon)`, `video-processing-service` running locally on my laptop can be treated as an _"external video processing provider"_.

- `Public Network`: Consists of `AWS EC2 Instance A`.