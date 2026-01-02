package com.b4s1ccoder.video_processing_service.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b4s1ccoder.common.enums.VideoStatus;
import com.b4s1ccoder.video_processing_service.health.WorkerState;
import com.b4s1ccoder.video_processing_service.model.Video;
import com.b4s1ccoder.video_processing_service.repository.VideoRepository;

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

  @Value("${app.buckets.raw}")
  private String rawBucket;

  @Value("${app.buckets.streams}")
  private String streamsBucket;

  @Transactional
  public void process(String bucket, String key) throws Exception {
    Video video = videoRepository.findByS3Key(key)
      .orElseThrow(() -> new IllegalStateException(
        "No video found for s3Key = " + key
      ));

    workerState.markWorking(key);

    try {
      video.setStatus(VideoStatus.PROCESSING);
      videoRepository.save(video);

      Path input = download(bucket, key);
      Path outputDir = Files.createTempDirectory("hls-");

      runFfmpeg(input, outputDir);
      uploadToS3(outputDir, key);
      cleanup(input, outputDir);

      video.setStatus(VideoStatus.READY);
      videoRepository.save(video);
    } catch (Exception e) {
      log.error("Video processing failed for {}", key, e);

      video.setStatus(VideoStatus.FAILED);
      videoRepository.save(video);

      throw e;
    } finally {
      workerState.markIdle();
    }
  }

  // Download raw video from s3
  private Path download(String bucket, String key) throws IOException {
    Path tempFile = Files.createTempFile("raw-", ".mp4");
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
  private void runFfmpeg(Path input, Path outputDir) throws Exception {
    // We use the FFmpeg cli because, it would be more efficient and fast to do the heavy
    // video processing work on CPU Threads, rather than using a Java wrapper that might
    // do this work on Java threads.

    // Moreover, it is easier for me to lookup an FFmpeg command than to learn the wrapper's API
    // the goal of this project is to learn about and creating a video processing pipeline used
    // in Streaming Platforms like Netflix, Prime Video, JioHotstar etc. NOT the actual conversion
    // process itself.

    // The FFmpeg command that: raw video -> FFmpeg -> 1080p, 720p, 480p, 240p
    List<String> command = List.of(
      "ffmpeg",
      "-y",
      "-threads", "0", // FFmpeg automatically chooses the number of threads
      "-i", input.toString(),
      "-filter_complex",
      "[0:v]split=4[v1080][v720][v480][v240];" +
      "[v1080]scale=1920:1080[v1080out];" +
      "[v720]scale=1280:720[v720out];" +
      "[v480]scale=854:480[v480out];" +
      "[v240]scale=426:240[v240out]",
      "-map", "[v1080out]", "-map", "0:a",
      "-map", "[v720out]", "-map", "0:a",
      "-map", "[v480out]", "-map", "0:a",
      "-map", "[v240out]", "-map", "0:a",
      "-c:v", "libx264",
      "-c:a", "aac",
      "-ar", "48000",
      "-b:v:0", "5000k",
      "-b:v:1", "2800k",
      "-b:v:2", "1400k",
      "-b:v:3", "400k",
      "-f", "hls", // Output format HTTP Live Streaming, widely-used for adaptive bitrate streaming
      "-hls_time", "6",
      "-hls_playlist_type", "vod",
      "-hls_flags", "independent_segments",
      "-hls_segment_filename",
      outputDir.resolve("out_%v/segment_%03d.ts").toString(), // The video chunks (of corresponding stream)
      "-master_pl_name", "master.m3u8", // master.m3u8 tells which streams are available
      "-var_stream_map",
      "v:0,a:0,name:1080p v:1,a:0,name:720p v:2,a:0,name:480p v:3,a:0,name:240p",
      outputDir.resolve("out_%v/index.m3u8").toString() // .m3u8 for particular stream
    );

    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);

    Process process = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      reader.lines().forEach(log::info); // Log, whatever FFmpeg outputs
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
