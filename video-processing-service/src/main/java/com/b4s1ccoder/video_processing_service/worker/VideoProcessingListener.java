package com.b4s1ccoder.video_processing_service.worker;

import org.springframework.stereotype.Component;

import com.b4s1ccoder.video_processing_service.service.VideoProcessingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingListener {

  private final ObjectMapper objectMapper;
  private final VideoProcessingService processingService;

  @SqsListener("processing-queue")
  public void handleMessage(String message) throws Exception {
    JsonNode root = objectMapper.readTree(message);

    // Ignore test events
    if (root.has("Event") && "s3:TestEvent".equals(root.get("Event").asText())) {
      log.info("S3 Test Event Ignored.");
      return;
    }

    JsonNode record = root.get("Records").get(0);
    String bucket = record.get("s3").get("bucket").get("name").asText();
    String key = record.get("s3").get("object").get("key").asText();

    log.info("Processing raw video s3://{}/{}", bucket, key);
    processingService.process(bucket, key);
  }
}
