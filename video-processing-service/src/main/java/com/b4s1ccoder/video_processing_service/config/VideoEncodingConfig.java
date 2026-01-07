package com.b4s1ccoder.video_processing_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "video.encoding")
@Data
public class VideoEncodingConfig {
  private String mode = "auto"; // auto, gpu, cpu
  private PresetConfig preset = new PresetConfig();

  @Data
  public static class PresetConfig {
    private String gpu = "p4";
    private String cpu = "faster";
  }
}
