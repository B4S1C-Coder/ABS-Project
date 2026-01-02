package com.b4s1ccoder.video_processing_service.queue;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

@Component
@RequiredArgsConstructor
public class QueueResolver {
  private final SqsClient sqsClient;
  private String processingQueueUrl;

  @PostConstruct
  void init() {
    processingQueueUrl = sqsClient.getQueueUrl(
      GetQueueUrlRequest.builder()
        .queueName("processing-queue")
      .build()
    ).queueUrl();
  }

  public String processingQueueUrl() {
    return processingQueueUrl;
  }
}
