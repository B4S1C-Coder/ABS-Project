package com.b4s1ccoder.video_processing_service.health;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class WorkerState {

  private final AtomicReference<WorkerStatus> status = new AtomicReference<>(WorkerStatus.IDLE);
  private volatile String currentVideoKey;

  public WorkerStatus getStatus() {
    return status.get();
  }

  public void markWorking(String videoKey) {
    this.currentVideoKey = videoKey;
    status.set(WorkerStatus.WORKING);
  }

  public void markIdle() {
    this.currentVideoKey = null;
    status.set(WorkerStatus.IDLE);
  }

  public Optional<String> getCurrentVideoKey() {
    return Optional.ofNullable(currentVideoKey);
  }
}
