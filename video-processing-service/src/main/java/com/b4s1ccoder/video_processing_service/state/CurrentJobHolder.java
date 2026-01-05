package com.b4s1ccoder.video_processing_service.state;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class CurrentJobHolder {
  private volatile UUID jobId;

  public void set(UUID jobId) {
    this.jobId = jobId;
  }

  public UUID get() {
    return jobId;
  }

  public void clear() {
    this.jobId = null;
  }
}
