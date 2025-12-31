package com.b4s1ccoder.upload_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b4s1ccoder.common.dto.CompleteMultipartUploadDTO;
import com.b4s1ccoder.common.dto.InitiateMultipartUploadDTO;
import com.b4s1ccoder.common.dto.InitiateMultipartUploadResponseDTO;
import com.b4s1ccoder.common.dto.IntermediateMultipartUploadDTO;
import com.b4s1ccoder.common.dto.PreSignedUploadUrlDTO;
import com.b4s1ccoder.common.dto.VideoUploadDTO;
import com.b4s1ccoder.upload_service.service.VideoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/uploads") // path: /internal/v1.0/uploads
@RequiredArgsConstructor
public class VideoController {

  private final VideoService videoService;

  @GetMapping(path = "/initiate", version = "1.0")
  public ResponseEntity<PreSignedUploadUrlDTO> initiateUploadSimple(@Valid @RequestBody VideoUploadDTO req) {
    String presignedUrl = videoService.initiateUpload(req);
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(PreSignedUploadUrlDTO.builder().uploadUrl(presignedUrl).build());
  }

  @GetMapping(path = "/initiate", version = "2.0")
  public ResponseEntity<InitiateMultipartUploadResponseDTO> initiateUploadMultipart(
    @Valid @RequestBody InitiateMultipartUploadDTO req
  ) {
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(videoService.initiateMultipartUpload(req));
  }

  @PostMapping(path = "/sign-part", version = "2.0")
  public ResponseEntity<PreSignedUploadUrlDTO> signPart(
    @Valid @RequestBody IntermediateMultipartUploadDTO req
  ) {

    String uploadUrl = videoService.getPresignedUrlForPart(req.getKey(), req.getUploadId(), req.getPartNumber());

    return ResponseEntity
      .status(HttpStatus.OK)
      .body(PreSignedUploadUrlDTO.builder().uploadUrl(uploadUrl).build());
  }

  @PostMapping(path = "/complete", version = "2.0")
  public ResponseEntity<Void> complete(@Valid @RequestBody CompleteMultipartUploadDTO req) {
    videoService.completeUpload(req.getKey(), req.getUploadId(), req.getParts());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
