package com.b4s1ccoder.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
public class IntermediateMultipartUploadDTO {
  @NotBlank(message = "key cannot be blank")
  private String key;

  @NotBlank(message = "uploadId cannot be blank")
  private String uploadId;

  @Positive
  private int partNumber;
}
