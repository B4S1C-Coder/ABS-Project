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

  @Value("${app.cdn.use-cdn:false}")
  private boolean useCdn;

  @Value("${app.buckets.streams}")
  private String streamsBucket;

  private String cdnDomain;

  @PostConstruct
  public void init() {
    if (useCdn) {
      try {
        String distributionId = ssmClient.getParameter(
          GetParameterRequest.builder()
            .name("/app/cloudfront-distribution-id")
          .build()
        ).parameter().value();

        this.cdnDomain = String.format(
          "http://%s.cloudfront.localhost.localstack.cloud:4566", distributionId
        );
        log.info("CDN Enabled. Serving from: {}", this.cdnDomain);
      } catch (Exception e) {
        log.error("Failed to fetch CDN ID from SSM. Falling back to S3 direct.", e);
        this.useCdn = false;
      }
    }
  }

  public String getManifestUrl(String videoId) {
    String path = "/streams/" + videoId + "/master.m3u8";

    if (useCdn && cdnDomain != null) {
      return cdnDomain + path;
    }

    return String.format(
      "http://%s.s3.localhost.localstack.cloud:4566%s", streamsBucket, path
    );
  }
}
