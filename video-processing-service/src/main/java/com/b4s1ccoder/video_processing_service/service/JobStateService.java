package com.b4s1ccoder.video_processing_service.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b4s1ccoder.common.enums.VideoStatus;
import com.b4s1ccoder.video_processing_service.config.WorkerId;
import com.b4s1ccoder.video_processing_service.model.VideoProcessingJob;
import com.b4s1ccoder.video_processing_service.repository.VideoProcessingJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobStateService {
  private final VideoProcessingJobRepository videoProcessingJobRepository;
  private final WorkerId workerId;

  @Transactional
  public boolean claimJob(VideoProcessingJob job) {
    return videoProcessingJobRepository.claimJob(
      job.getId(), workerId.getId(), LocalDateTime.now().plusSeconds(60)
    );
  }

  @Transactional
  public void markReady(VideoProcessingJob job) {
    job.setStatus(VideoStatus.READY);
    videoProcessingJobRepository.save(job);
  }

  @Transactional
  public void markFailed(VideoProcessingJob job) {
    job.setStatus(VideoStatus.FAILED);
    videoProcessingJobRepository.save(job);
  }
}
