package com.b4s1ccoder.upload_service.mapper;

import com.b4s1ccoder.common.dto.InitiateMultipartUploadDTO;
import com.b4s1ccoder.common.dto.VideoUploadDTO;
import com.b4s1ccoder.upload_service.model.Video;

public class VideoMapper {
  public static Video toVideoObj(String key, VideoUploadDTO dto) {
    return Video.builder()
      .s3Key(key)
      .originalFilename(dto.getFilename())
      .title(dto.getTitle())
      .description(dto.getDescription())
    .build();
  }

  public static Video toVideoObj(String key, InitiateMultipartUploadDTO dto) {
    return Video.builder()
      .s3Key(key)
      .originalFilename(dto.getFilename())
      .title(dto.getTitle())
      .description(dto.getDescription())
    .build();
  }
}
