package com.b4s1ccoder.video_processing_service.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.b4s1ccoder.common.enums.VideoStatus;
import com.b4s1ccoder.video_processing_service.model.VideoProcessingJob;

public interface VideoProcessingJobRepository
    extends JpaRepository<VideoProcessingJob, UUID> {
  
  Optional<VideoProcessingJob> findByVideoId(UUID videoId);
  Optional<VideoProcessingJob> findByVideoIdAndStatus(
    UUID videoId, VideoStatus status
  );

  @Modifying
  @Query("""
    UPDATE VideoProcessingJob j
      SET j.leaseUntil = :leaseUntil
    WHERE j.id = :jobId
      AND j.workerId = :workerId
      AND j.status = 'PROCESSING'
  """)
  int extendLease(
    @Param("jobId") UUID jobId,
    @Param("workerId") String workerId,
    @Param("leaseUntil") LocalDateTime leaseUntil
  );
}
