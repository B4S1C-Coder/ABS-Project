package com.b4s1ccoder.video_processing_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "video.segment")
@Data
public class VideoSegmentConfig {
  private int duration = 10;
}
