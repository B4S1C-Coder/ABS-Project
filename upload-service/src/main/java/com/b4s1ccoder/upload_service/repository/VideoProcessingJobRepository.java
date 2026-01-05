package com.b4s1ccoder.upload_service.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.b4s1ccoder.common.enums.VideoStatus;
import com.b4s1ccoder.upload_service.model.VideoProcessingJob;

public interface VideoProcessingJobRepository
    extends JpaRepository<VideoProcessingJob, UUID> {
  
  Optional<VideoProcessingJob> findByVideoId(UUID videoId);
  Optional<VideoProcessingJob> findByVideoIdAndStatus(
    UUID videoId, VideoStatus status
  );
}
