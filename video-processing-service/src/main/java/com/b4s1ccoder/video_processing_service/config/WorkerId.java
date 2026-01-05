package com.b4s1ccoder.video_processing_service.config;

import java.util.UUID;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WorkerId {
  private final String id = UUID.randomUUID().toString();

  @PostConstruct
  public void init() {
    log.info("Worker started with workerId={}", id);
  }

  public String getId() {
    return id;
  }
}
