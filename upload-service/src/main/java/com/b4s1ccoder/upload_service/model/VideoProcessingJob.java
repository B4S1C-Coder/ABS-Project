package com.b4s1ccoder.upload_service.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.b4s1ccoder.common.enums.VideoStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
  name = "video_processing_jobs",
  uniqueConstraints = {
    @UniqueConstraint(name = "uq_video_processing_jobs_video", columnNames = "video_id")
  },
  indexes = {
    @Index(name = "idx_video_processing_jobs_lease", columnList = "lease_until")
  }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoProcessingJob {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "video_id", nullable = false)
  private Video video;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private VideoStatus status;

  @Column(name = "worker_id")
  private String workerId;

  @Column(name = "lease_until")
  private LocalDateTime leaseUntil;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
