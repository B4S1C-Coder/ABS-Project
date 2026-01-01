package com.b4s1ccoder.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.extern.jackson.Jacksonized;

@Data
@Builder
// @Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class InitiateMultipartUploadResponseDTO {
  private String uploadId;
  private String key;
}
