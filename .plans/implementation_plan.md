# Pipeline Metrics and MOCK Processing Plan

## Goal Description
Implement a MOCK mode in `video-processing-service` to simulate video transcoding based on defined target speed characteristics (without relying on actual FFmpeg usage or S3 uploads). This bypasses the need for high-end hardware. Then, orchestrate dynamic scaling of worker nodes and run a Node.js benchmark script (`web/benchmark.js`) to capture:
1. Encoding Speed Ratio (Real-Time Factor)
2. Mean Time to Recovery (MTTR) for failed jobs
3. Queue Dwell Time
4. Duplicate Processing Rate (Lock Contention)

## Proposed Changes

### Configuration Updates
#### [MODIFY] [application.yaml](file:///home/saksham/codebase/streaming-platform/video-processing-service/src/main/resources/application.yaml)
- Add mock configuration section:
```yaml
app:
  ...
  mock:
    enabled: ${MOCK_ENABLED:false}
    encoding-speed-ratio: ${MOCK_ENCODING_SPEED_RATIO:0.5}
    base-video-duration-sec: ${MOCK_BASE_VIDEO_DURATION_SEC:60}
    crash-probability: ${MOCK_CRASH_PROBABILITY:0.0}
```

### Video Processing Module
#### [MODIFY] [VideoProcessingService.java](file:///home/saksham/codebase/streaming-platform/video-processing-service/src/main/java/com/b4s1ccoder/video_processing_service/service/VideoProcessingService.java)
- Inject mock configurations (`app.mock.*`).
- In the `process(...)` method, branch logic if `mock.enabled` is true:
  - Generate a random video duration around the base duration.
  - Calculate `processing_time = video_duration / encoding_speed_ratio`.
  - Check against `mock.crash-probability`. If triggered randomly, use `Runtime.getRuntime().halt(1)` to simulate a hard crash (leaving the lease hanging to test MTTR).
  - Use `Thread.sleep(processing_time * 1000)` to simulate the FFmpeg delay.
  - Skip S3 downloads, FFmpeg executions, and S3 uploads.
  - Finally, mark the job as `READY` through `jobStateService`.

### Infrastructure Updates
#### [MODIFY] [docker-compose.yml](file:///home/saksham/codebase/streaming-platform/infra/local/docker-compose.yml)
- Change `container_name: sp-video-processing-service` to allow dynamic scaling.
- Comment out or conditionally pass `deploy` GPU reservations so multiple mocked instances can scale on arbitrary hardware.
- Pass `MOCK_` environment variables with default benchmark values.

### Benchmark Script
#### [NEW] [benchmark.js](file:///home/saksham/codebase/streaming-platform/web/benchmark.js)
- Build a script using `fetch` or `axios` to coordinate API tests leveraging `ui/sp-ui/lib/api.ts` mechanics.
- Pre-requisites: Start DB, upload service, and `N` instances of `sp-video-processing-service`.
- Execution flow:
  1. Trigger 10-20 uploads in parallel or sequentially.
  2. Implement a polling loop on `GET /upload/all-jobs`.
  3. Calculate metrics:
     - **Queue Dwell Time**: Track `createdAt` versus the time status flips from `PENDING` to `PROCESSING`.
     - **MTTR**: Track instances where a job is stuck in `PROCESSING` but the `workerId` switches after a timeout, or calculate total time beyond expected processing duration.
     - **Lock Contention**: Since claims are handled entirely via DB (`jobStateService.claimJob`), monitor duplicate claim attempts (we can add a Redis counter or simply deduce it via DB query counts). Alternatively, track if a video ever receives >1 processing success events. Wait, the DB enforces unique claims (`claim_video_processing_job`), so lock contention can be validated by observing 0 double-processing completions.
     - **Encoding Speed Ratio**: Calculated via Mock configuration values internally, but observable via (Processing End Time - Claim Time).
  4. Output the metrics in a structured console format.

## Verification Plan

### Automated Tests
- N/A for this task since we are building a benchmark. The benchmark itself is the test.

### Manual Verification
1. `docker-compose up -d --scale sp-video-processing-service=10`
2. Ensure Mock variables are picked up, `MOCK_ENABLED=true` and `MOCK_CRASH_PROBABILITY=0.10` (10% chance).
3. `node web/benchmark.js` 
4. Verify console output returns metrics:
   - Queue Dwell Time averages ~ X seconds
   - Instances of Worker Crashing successfully triggered MTTR calculation.
   - 0% Duplicate Processing Rate.

All documentation matching these details will also be stored in `.plans/` as requested explicitly by the user.
