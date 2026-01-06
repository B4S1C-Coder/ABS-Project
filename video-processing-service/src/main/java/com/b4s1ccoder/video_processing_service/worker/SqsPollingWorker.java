package com.b4s1ccoder.video_processing_service.worker;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.b4s1ccoder.video_processing_service.queue.QueueResolver;
import com.b4s1ccoder.video_processing_service.service.VideoProcessingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsPollingWorker {
  private final SqsClient sqsClient;
  private final QueueResolver queueResolver;
  private final VideoProcessingService processingService;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 2000)
  public void poll() {
    ReceiveMessageResponse response = sqsClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(queueResolver.processingQueueUrl())
        .maxNumberOfMessages(1)
        .waitTimeSeconds(10)
        .visibilityTimeout(30) // 15 minutes --> 900
      .build()
    );

    if (response.messages().isEmpty()) {
      return;
    }

    Message message = response.messages().getFirst();

    try {
      boolean processed = handleMessage(message);
      
      if (processed) {
        sqsClient.deleteMessage(
          DeleteMessageRequest.builder()
            .queueUrl(queueResolver.processingQueueUrl())
            .receiptHandle(message.receiptHandle())
          .build()
        );
      }
    } catch (Exception e) {
      log.error("Message processing failed.", e);
    }
  }

  private boolean handleMessage(Message message) throws Exception {
    JsonNode root = objectMapper.readTree(message.body());

    if (root.has("Event") && "s3:TestEvent".equals(root.get("Event").asText())) {
      log.info("Test s3 event ignored.");
      return true;
    }

    JsonNode record = root.get("Records").get(0);
    String bucket = record.get("s3").get("bucket").get("name").asText();
    String key = record.get("s3").get("object").get("key").asText();

    log.info("Discovered s3://{}/{}", bucket, key);
    try {
      boolean claimed = processingService.process(bucket, key);
      if (!claimed) {
        log.info("Job not claimed, leaving SQS Message as it is ...");
      }
      return claimed;
    } catch (IllegalStateException e) {
      log.info("Skipping {} (job not claimable): {}", key, e.getMessage());
      return true;
    }
  }
}
