package com.b4s1ccoder.video_processing_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.mock")
@Data
public class MockConfig {
  private boolean enabled = false;
  private double encodingSpeedRatio = 0.5;
  private int baseVideoDurationSec = 60;
  private double crashProbability = 0.1;
}
