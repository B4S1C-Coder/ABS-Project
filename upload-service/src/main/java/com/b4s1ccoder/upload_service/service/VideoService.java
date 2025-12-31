package com.b4s1ccoder.upload_service.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.b4s1ccoder.common.dto.InitiateMultipartUploadDTO;
import com.b4s1ccoder.common.dto.InitiateMultipartUploadResponseDTO;
import com.b4s1ccoder.common.dto.PartDTO;
import com.b4s1ccoder.common.dto.VideoUploadDTO;
import com.b4s1ccoder.upload_service.mapper.VideoMapper;
import com.b4s1ccoder.upload_service.model.Video;
import com.b4s1ccoder.upload_service.repository.VideoRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

@Service
@RequiredArgsConstructor
public class VideoService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final VideoRepository videoRepository;

  @Value("${spring.cloud.aws.s3.bucket-name}")
  private String bucketName;

  // Standard PUT Upload, only works for files < 5GB
  public String initiateUpload(VideoUploadDTO req) {
    String key = UUID.randomUUID().toString() + "-" + req.getFilename();
    
    Video video = VideoMapper.toVideoObj(key, req);
    videoRepository.save(video);

    PutObjectRequest objectRequest = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .contentType("video/mp4")
    .build();

    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
      .signatureDuration(Duration.ofMinutes(30))
      .putObjectRequest(objectRequest)
    .build();

    return s3Presigner.presignPutObject(presignRequest).url().toString();
  }

  // Inititate Multipart Upload
  public InitiateMultipartUploadResponseDTO initiateMultipartUpload(InitiateMultipartUploadDTO req) {
    String key = UUID.randomUUID().toString() + "-" + req.getFilename();

    Video video = VideoMapper.toVideoObj(key, req);
    videoRepository.save(video);

    // Create the multipart request and inform s3 to prepare for an incoming multipart upload
    CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
      .bucket(bucketName)
      .key(key)
      .contentType("video/mp4")
    .build();

    CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createRequest);
    
    return InitiateMultipartUploadResponseDTO.builder()
      .uploadId(response.uploadId())
      .key(key)
    .build();
  }

  // Pre-Signed URL for each chunk
  public String getPresignedUrlForPart(String key, String uploadId, int partNumber) {
    UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
      .bucket(bucketName)
      .key(key)
      .uploadId(uploadId)
      .partNumber(partNumber)
    .build();

    UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
      .signatureDuration(Duration.ofMinutes(10))
      .uploadPartRequest(uploadPartRequest)
    .build();

    PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);
    return presignedRequest.url().toString();
  }

  // All chunks uploaded, stich em all
  public void completeUpload(String key, String uploadId, List<PartDTO> parts) {
    List<CompletedPart> completedParts = parts.stream()
      .map(
        part -> CompletedPart.builder()
          .partNumber(part.getPartNumber())
          .eTag(part.getETag())
        .build()
      )
    .collect(Collectors.toList());

    CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
      .parts(completedParts)
    .build();

    CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
      .bucket(bucketName)
      .key(key)
      .uploadId(uploadId)
      .multipartUpload(completedMultipartUpload)
    .build();

    s3Client.completeMultipartUpload(completeRequest);
    // After this, s3 stiches all the chunks to get the large raw video file.
    // Then s3 pushes a message to SQS, which will then be pulled by a
    // video processing worker which, raw video -> 1080p, 720p, 480p
  }
}
