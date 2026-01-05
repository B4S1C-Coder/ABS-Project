package com.b4s1ccoder.upload_service.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.b4s1ccoder.upload_service.model.Video;

public interface VideoRepository extends JpaRepository<Video, UUID> {
  Optional<Video> findByS3Key(String s3Key);
}
