package com.b4s1ccoder.video_processing_service.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// Integrate with actuator
@Component
@RequiredArgsConstructor
public class VideoWorkerHealthIndicator implements HealthIndicator {
  private final WorkerState workerState;

  @Override
  public Health health() {
    Health.Builder builder = Health.up();
    builder.withDetail("workerStatus", workerState.getStatus());

    workerState.getCurrentVideoKey()
      .ifPresent(key -> builder.withDetail("currentVideo", key));
    
    return builder.build();
  }
}
