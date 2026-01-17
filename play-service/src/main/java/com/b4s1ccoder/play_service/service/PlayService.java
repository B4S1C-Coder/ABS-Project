package com.b4s1ccoder.play_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayService {
  private final SsmClient ssmClient;

  @Value("${app.cdn.enabled:false}")
  private boolean useCdn;

  @Value("${app.buckets.streams}")
  private String streamsBucket;

  @Value("${app.cdn.base-url:http://localhost:8080}")
  private String cdnBaseUrl;

  @PostConstruct
  public void init() {
    if (useCdn) {
      log.info("CDN enabled. Base URL: {}", cdnBaseUrl);

      try {
        String distributionId = ssmClient.getParameter(
          GetParameterRequest.builder()
            .name("/app/cloudfront-distribution-id")
          .build()
        ).parameter().value();

        log.info("Loaded CDN distribution ID: {}", distributionId);
      } catch (Exception e) {
        log.error("Failed to fetch CDN ID from SSM. Falling back to S3 direct.", e);
        this.useCdn = false;
      }
    }
  }

  public String getManifestUrl(String videoId) {
    String path = "/video/" + videoId + "/master.m3u8";

    if (useCdn && cdnBaseUrl != null && !cdnBaseUrl.isBlank()) {
      return cdnBaseUrl + path;
    }

    return String.format(
      "http://%s.s3.localhost.localstack.cloud:4566/streams/%s/master.m3u8",
      streamsBucket, videoId
    );
  }
}
