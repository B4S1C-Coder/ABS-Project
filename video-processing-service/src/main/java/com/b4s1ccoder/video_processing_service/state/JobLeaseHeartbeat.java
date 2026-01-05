package com.b4s1ccoder.video_processing_service.state;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.b4s1ccoder.video_processing_service.config.WorkerId;
import com.b4s1ccoder.video_processing_service.repository.VideoProcessingJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobLeaseHeartbeat {

  private static final Duration LEASE_EXTENSION = Duration.ofMinutes(2);
  private final CurrentJobHolder currentJobHolder;
  private final VideoProcessingJobRepository jobRepository;
  private final WorkerId workerId;

  @Scheduled(fixedDelay = 30_000)
  @Transactional
  public void heartbeat() {
    UUID jobId = currentJobHolder.get();

    if (jobId == null) {
      return; // Worker is idle
    }

    int updated = jobRepository.extendLease(
      jobId, workerId.getId(), LocalDateTime.now().plus(LEASE_EXTENSION)
    );

    if (updated == 0) {
      log.warn("Failed to renew lease for job {}", jobId);
    } else {
      log.debug("Lease renewed for job {}", jobId);
    }
  }
}
