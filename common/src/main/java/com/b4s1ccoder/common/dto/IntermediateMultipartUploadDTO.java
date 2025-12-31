package com.b4s1ccoder.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class IntermediateMultipartUploadDTO {
  @NotBlank(message = "key cannot be blank")
  private String key;

  @NotBlank(message = "uploadId cannot be blank")
  private String uploadId;

  @Positive
  private int partNumber;
}
