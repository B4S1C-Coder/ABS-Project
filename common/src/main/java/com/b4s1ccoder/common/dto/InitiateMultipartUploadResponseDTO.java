package com.b4s1ccoder.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class InitiateMultipartUploadResponseDTO {
  private String uploadId;
  private String key;
}
