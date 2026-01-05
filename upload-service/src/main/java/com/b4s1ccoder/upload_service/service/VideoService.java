package com.b4s1ccoder.upload_service.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b4s1ccoder.common.dto.InitiateMultipartUploadDTO;
import com.b4s1ccoder.common.dto.InitiateMultipartUploadResponseDTO;
import com.b4s1ccoder.common.dto.PartDTO;
import com.b4s1ccoder.common.dto.VideoUploadDTO;
import com.b4s1ccoder.common.enums.VideoStatus;
import com.b4s1ccoder.upload_service.mapper.VideoMapper;
import com.b4s1ccoder.upload_service.model.Video;
import com.b4s1ccoder.upload_service.model.VideoProcessingJob;
import com.b4s1ccoder.upload_service.repository.VideoProcessingJobRepository;
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
  private final VideoProcessingJobRepository videoProcessingJobRepository;

  @Value("${spring.cloud.aws.s3.bucket-name}")
  private String bucketName;

  // Standard PUT Upload, only works for files < 5GB, deprecated do not use
  @Deprecated
  @Transactional
  public String initiateUpload(VideoUploadDTO req) {
    String key = UUID.randomUUID().toString() + "-" + req.getFilename();
    
    Video video = VideoMapper.toVideoObj(key, req);
    Video savedVideo = videoRepository.save(video);

    VideoProcessingJob job = VideoProcessingJob.builder()
      .video(savedVideo)
      .status(VideoStatus.UPLOADING)
    .build();

    videoProcessingJobRepository.save(job);

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
  @Transactional
  public InitiateMultipartUploadResponseDTO initiateMultipartUpload(InitiateMultipartUploadDTO req) {
    String key = UUID.randomUUID().toString() + "-" + req.getFilename();

    Video video = VideoMapper.toVideoObj(key, req);
    Video savedVideo = videoRepository.save(video);

    VideoProcessingJob job = VideoProcessingJob.builder()
      .video(savedVideo)
      .status(VideoStatus.UPLOADING)
    .build();

    videoProcessingJobRepository.save(job);

    // Create the multipart request and inform s3 to prepare for an incoming multipart upload
    CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
      .bucket(bucketName)
      .key(key)
      .contentType("video/mp4")
    .build();

    CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createRequest);
    return new InitiateMultipartUploadResponseDTO(response.uploadId(), key);
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
  @Transactional
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

    Video video = videoRepository.findByS3Key(key).orElseThrow(
      () -> new IllegalStateException("Video is missing.")
    );

    VideoProcessingJob job = videoProcessingJobRepository.findByVideoIdAndStatus(
      video.getId(), VideoStatus.UPLOADING
    ).orElseThrow(
      () -> new IllegalStateException("Job is missing.")
    );

    job.setStatus(VideoStatus.PENDING);
    videoProcessingJobRepository.save(job);
  }
}
