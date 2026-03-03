# Metrics Tracking Plan

## Overview
Track specific metrics within the streaming-platform mock setup as worker instances scale from 1 to 50.

## Metrics
1. **Encoding Speed Ratio (Real-Time Factor)**: Ratio of video duration to encoding time.
2. **Mean Time to Recovery (MTTR) for Failed Jobs**: Time for lease to expire and another worker to pick up if a worker crashes mid-transcode.
3. **Queue Dwell Time**: Time a video sits in SQS before a worker claims it.
4. **Duplicate Processing Rate**: Percentage of jobs accidentally processed twice (Lock contention).

## Implementation Steps
1. **Mock Mode (`video-processing-service`)**
   - Instead of running FFMPEG, sleep for a random duration (base duration determined by encoding speed ratio + random time delta).
   - Add a simulated crash mode to randomly exit the process while processing to test MTTR.

2. **Scaling Mechanism**
   - Modify `docker-compose.yml` to support scaling the `video-processing-service`. Ensure no static port conflicts prevent multiple instances.

3. **Benchmarking Script (`web/benchmark.js`)**
   - Orchestrate uploads (referencing `ui/sp-ui/lib/api.ts` and `web/local-test-client.js`).
   - Monitor and record Queue Dwell Time, MTTR, Duplicate Processing Rate, and Encoding Speed Ratio.
   - Run tests varying instance count from 1 to 50 using `docker-compose up --scale video-processing-service=N`.
