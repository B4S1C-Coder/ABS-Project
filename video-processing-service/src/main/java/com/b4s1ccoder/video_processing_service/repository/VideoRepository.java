package com.b4s1ccoder.video_processing_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.b4s1ccoder.video_processing_service.model.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
  Optional<Video> findByS3Key(String s3Key);
}
