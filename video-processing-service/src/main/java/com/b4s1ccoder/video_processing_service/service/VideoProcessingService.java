package com.b4s1ccoder.video_processing_service.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.b4s1ccoder.video_processing_service.config.VideoEncodingConfig;
import com.b4s1ccoder.video_processing_service.config.VideoSegmentConfig;
import com.b4s1ccoder.video_processing_service.config.WorkerId;
import com.b4s1ccoder.video_processing_service.health.WorkerState;
import com.b4s1ccoder.video_processing_service.model.Video;
import com.b4s1ccoder.video_processing_service.model.VideoProcessingJob;
import com.b4s1ccoder.video_processing_service.repository.VideoProcessingJobRepository;
import com.b4s1ccoder.video_processing_service.repository.VideoRepository;
import com.b4s1ccoder.video_processing_service.state.CurrentJobHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingService {
  private final S3Client s3Client;
  private final WorkerState workerState;
  private final VideoRepository videoRepository;
  private final VideoProcessingJobRepository videoProcessingJobRepository;
  private final WorkerId workerId;
  private final CurrentJobHolder currentJobHolder;
  private final VideoEncodingConfig encodingConfig;
  private final VideoSegmentConfig segmentConfig;
  private final JobStateService jobStateService;

  private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

  // Will be determined if GPU is available or not, decision is cached
  private Boolean useGpu = null;

  private List<String> buildFfmpegCommand(Path input, Path outputDir) {
    if (useGpu == null) {
      useGpu = determineEncodingMode();
    }

    if (useGpu) {
      log.info("Using GPU-accelerated encoding (NVENC)");
      return buildGpuCommand(input, outputDir);
    } else {
      log.info("Using CPU encoding (libx264)");
      return buildCpuCommand(input, outputDir);
    }
  }

  // FOR SIMPLICITY I AM ONLY CONSIDERING NVIDIA GPUs, SINCE THAT IS WHAT
  // MY LAPTOP HAS
  private boolean determineEncodingMode() {
    String mode = encodingConfig.getMode().toLowerCase();

    // Check if NVENC is available
    boolean nvencAvailable = false;
    try {
      ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-encoders");
      Process process = pb.start();

      try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream())
      )) {
        nvencAvailable = reader.lines().anyMatch(
          line -> line.contains("h264_nvenc")
        );
      }

    } catch (IOException e) {
      log.error("Failed to check for NVENC: {}", e.getMessage());
    }

    // Consider the provided configs
    boolean shouldUseGpu = switch (mode) {
      case "gpu" -> {
        if (!nvencAvailable) {
          log.warn("GPU mode is set but NVENC not available, will use CPU");
        }
        yield nvencAvailable;
      }
      case "cpu" -> false;
      default -> nvencAvailable;
    };

    log.info("Encoding mode determined: {} (NVENC available: {})", 
                 shouldUseGpu ? "GPU" : "CPU", nvencAvailable);
    return shouldUseGpu;
  }

  private List<String> buildGpuCommand(Path input, Path outputDir) {
    return List.of(
      "ffmpeg",
      "-y",
      "-hwaccel", "cuda",
      "-hwaccel_output_format", "cuda",
      "-i", input.toString(),
      "-filter_complex",
      "[0:v]split=4[v1080][v720][v480][v240];" +
      "[v1080]scale_cuda=1920:1080[v1080out];" +
      "[v720]scale_cuda=1280:720[v720out];" +
      "[v480]scale_cuda=854:480[v480out];" +
      "[v240]scale_cuda=426:240[v240out]",
      "-map", "[v1080out]",
      "-map", "[v720out]",
      "-map", "[v480out]",
      "-map", "[v240out]",
      "-map", "0:a",
      "-c:v", "h264_nvenc",
      "-preset", encodingConfig.getPreset().getGpu(),
      "-c:a", "aac",
      "-ar", "48000",
      "-b:v:0", "5000k",
      "-b:v:1", "2800k",
      "-b:v:2", "1400k",
      "-b:v:3", "400k",
      "-b:a:0", "192k",
      "-f", "hls",
      "-hls_time", String.valueOf(segmentConfig.getDuration()),
      "-hls_playlist_type", "vod",
      "-hls_flags", "independent_segments",
      "-hls_segment_filename", outputDir.resolve("stream_%v/segment_%03d.ts").toString(),
      "-master_pl_name", "master.m3u8",
      "-var_stream_map", "v:0 v:1 v:2 v:3 a:0",
      outputDir.resolve("stream_%v/playlist.m3u8").toString()
    );
  }
    
  private List<String> buildCpuCommand(Path input, Path outputDir) {
    return List.of(
      "ffmpeg",
      "-y",
      "-i", input.toString(),
      "-filter_complex",
      "[0:v]split=4[v1080][v720][v480][v240];" +
      "[v1080]scale=1920:1080[v1080out];" +
      "[v720]scale=1280:720[v720out];" +
      "[v480]scale=854:480[v480out];" +
      "[v240]scale=426:240[v240out]",
      "-map", "[v1080out]",
      "-map", "[v720out]",
      "-map", "[v480out]",
      "-map", "[v240out]",
      "-map", "0:a",
      "-c:v", "libx264",
      "-preset", encodingConfig.getPreset().getCpu(),
      "-c:a", "aac",
      "-ar", "48000",
      "-b:v:0", "5000k",
      "-b:v:1", "2800k",
      "-b:v:2", "1400k",
      "-b:v:3", "400k",
      "-b:a:0", "192k",
      "-f", "hls",
      "-hls_time", String.valueOf(segmentConfig.getDuration()),
      "-hls_playlist_type", "vod",
      "-hls_flags", "independent_segments",
      "-hls_segment_filename", outputDir.resolve("stream_%v/segment_%03d.ts").toString(),
      "-master_pl_name", "master.m3u8",
      "-var_stream_map", "v:0 v:1 v:2 v:3 a:0",
      outputDir.resolve("stream_%v/playlist.m3u8").toString()
    );
  }

  @Value("${app.buckets.raw}")
  private String rawBucket;

  @Value("${app.buckets.streams}")
  private String streamsBucket;

  private ScheduledFuture<?> startHeartbeat(VideoProcessingJob job, AtomicBoolean leaseLost) {
    return heartbeatScheduler.scheduleAtFixedRate(() -> {
      try {
        int updated =  videoProcessingJobRepository.extendLease(
          job.getId(), workerId.getId(), LocalDateTime.now().plusSeconds(60)
        );

        if (updated == 0) {
          log.error("Lost lease for job {}", job.getId());
          leaseLost.set(true);
          // throw new IllegalStateException("Lease lost");
        }

        log.debug("Lease extended for job {}", job.getId());
      } catch (Exception e) {
        log.error("Heartbeat failed for job {}", job.getId(), e);
      }
    }, 20, 20, TimeUnit.SECONDS);
  }

  // Boolean return indicates whether job was actually claimed or not
  public boolean process(String bucket, String key) throws Exception {
    Video video = videoRepository.findByS3Key(key).orElseThrow(
      () -> new IllegalStateException("No video found for s3Key = " + key)
    );
    
    VideoProcessingJob job = videoProcessingJobRepository
      .findByVideoId(video.getId()).orElseThrow(
        () -> new IllegalStateException("No job found for s3Key = " + key)
      );
    
    // Job claiming moved to DB layer (lease extension also at DB layer)
    // As DB is the source of truth and authoritative regarding who owns
    // what job

    if (!jobStateService.claimJob(job)) {
      log.info("Job {} already claimed, skipping ...", job.getId());
      return false;
    }
    
    currentJobHolder.set(job.getId());
    workerState.markWorking(key);
    
    AtomicBoolean leaseLost = new AtomicBoolean(false);
    ScheduledFuture<?> heartbeat = startHeartbeat(job, leaseLost);

    try {
      Path input = download(bucket, key);
      Path outputDir = Files.createTempDirectory("hls-");

      runFfmpeg(input, outputDir, leaseLost);
      uploadToS3(outputDir, key);
      cleanup(input, outputDir);

      jobStateService.markReady(job);

      return true;

    } catch (Exception e) {
      log.error("Video processing failed for {}", key, e);
      jobStateService.markFailed(job);

      throw e;
    } finally {
      if (heartbeat != null) {
        heartbeat.cancel(true);
      }
      workerState.markIdle();
      currentJobHolder.clear();
    }
  }

  // Download raw video from s3
  private Path download(String bucket, String key) throws IOException {
    Path tempDir = Files.createTempDirectory("raw-video-");
    Path tempFile = tempDir.resolve("input.mp4");
    log.info("Downloading s3://{}/{}", bucket, key);

    s3Client.getObject(
      GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
      .build(),
      ResponseTransformer.toFile(tempFile)    
    );

    return tempFile;
  }

  // FFMPeg CLI must be installed in the environment wherever this service runs
  private void runFfmpeg(Path input, Path outputDir, AtomicBoolean leaseLost) throws Exception {
    // We use the FFmpeg cli because, it would be more efficient and fast to do the heavy
    // video processing work on CPU Threads, rather than using a Java wrapper that might
    // do this work on Java threads.

    // Moreover, it is easier for me to lookup an FFmpeg command than to learn the 
    // wrapper's API the goal of this project is to learn about and creating a video
    // processing pipeline used in Streaming Platforms like Netflix, Prime Video,
    // JioHotstar etc. NOT the actual conversion process itself.

    // The FFmpeg command that: raw video -> FFmpeg -> 1080p, 720p, 480p, 240p
    List<String> command = buildFfmpegCommand(input, outputDir);

    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);

    Process process = pb.start();

    try (BufferedReader reader = new BufferedReader(
      new InputStreamReader(process.getInputStream())
    )) {
      // reader.lines().forEach(log::info); // Log, whatever FFmpeg outputs
      String line;

      while ((line = reader.readLine()) != null) {
        log.info(line);

        if (leaseLost.get()) {
          log.error("Lease lost -- stopping FFmpeg");
          process.destroyForcibly();
          throw new IllegalStateException("Lease lost");
        }
      }
    }

    int exitCode = process.waitFor();

    // If FFmpeg fails
    if (exitCode != 0) {
      throw new IllegalStateException("FFmpeg failed with exit code: " + exitCode);
    }

    // Expected Directory structure after this process is completed.
    //     hls/
    // ├── master.m3u8
    // ├── out_0/
    // │   ├── index.m3u8   (1080p)
    // │   └── segment_000.ts
    // ├── out_1/           (720p)
    // ├── out_2/           (480p)
    // └── out_3/           (240p)
  }

  // Uploads the processed stuff to s3
  private void uploadToS3(Path outputDir, String originalKey) throws IOException {
    String videoId = originalKey.substring(0, originalKey.lastIndexOf('.'));

    // A regular file is a file that is not a directory or a special type of file
    // such as a symbolic link, pipe, socket, or device. It typically stores opaque
    // content (a sequence of bytes). 
    Files.walk(outputDir)
      .filter(Files::isRegularFile)
      .forEach(file -> {
        String key = "streams/" + videoId + "/" + outputDir.relativize(file);
        String contentType = file.toString().endsWith(".m3u8")
          ? "application/vnd.apple.mpegurl" : "video/MP2T";
        
          log.info("Uploading {}", key);

          s3Client.putObject(
            PutObjectRequest.builder()
              .bucket(streamsBucket)
              .key(key)
              .contentType(contentType)
            .build(),
            file
          );
      });
  }

  // Clean up, to free up disk space since:
  // raw video + 4 streams = Enough disk space that should not be ignored
  private void cleanup(Path input, Path outputDir) throws IOException {
    Files.deleteIfExists(input);

    Files.walk(outputDir)
      .sorted(Comparator.reverseOrder())
      .forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException ignored) {}
      });
  }
}
