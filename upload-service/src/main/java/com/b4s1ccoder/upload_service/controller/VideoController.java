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
// import com.b4s1ccoder.common.dto.VideoUploadDTO;
import com.b4s1ccoder.upload_service.service.VideoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/internal/uploads")
@RequiredArgsConstructor
public class VideoController {

  private final VideoService videoService;

  @GetMapping("/health")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("Upload service is up and running.");
  }

  // @PostMapping("/initiate-single")
  // public ResponseEntity<PreSignedUploadUrlDTO> initiateUploadSimple(@Valid @RequestBody VideoUploadDTO req) {
  //   String presignedUrl = videoService.initiateUpload(req);
  //   return ResponseEntity
  //     .status(HttpStatus.CREATED)
  //     .body(new PreSignedUploadUrlDTO(presignedUrl));
  // }

  @PostMapping("/initiate")
  public ResponseEntity<InitiateMultipartUploadResponseDTO> initiateUploadMultipart(
    @Valid @RequestBody InitiateMultipartUploadDTO req
  ) {
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(videoService.initiateMultipartUpload(req));
  }

  @PostMapping("/sign-part")
  public ResponseEntity<PreSignedUploadUrlDTO> signPart(
    @Valid @RequestBody IntermediateMultipartUploadDTO req
  ) {

    String uploadUrl = videoService.getPresignedUrlForPart(req.getKey(), req.getUploadId(), req.getPartNumber());

    return ResponseEntity
      .status(HttpStatus.OK)
      .body(new PreSignedUploadUrlDTO(uploadUrl));
  }

  @PostMapping("/complete")
  public ResponseEntity<Void> complete(@Valid @RequestBody CompleteMultipartUploadDTO req) {
    videoService.completeUpload(req.getKey(), req.getUploadId(), req.getParts());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
