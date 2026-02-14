package com.b4s1ccoder.play_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// import jakarta.annotation.PostConstruct;
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

  private volatile boolean cdnResolved = false;

  private synchronized void lazilyResolveCdn() {
    if (!useCdn || cdnResolved) return;

    log.info("Resolving CDN ...");

    try {
      String distributionId = ssmClient.getParameter(
        GetParameterRequest.builder()
          .name("/app/cloudfront-distribution-id")
        .build()
      ).parameter().value();

      cdnResolved = true;
      log.info("CDN resolved successfully: {}", distributionId);
    } catch (Exception e) {
      log.warn("CDN not ready, will retry ...");
    }
  }

  public String getManifestUrl(String videoId) {
    lazilyResolveCdn();

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
