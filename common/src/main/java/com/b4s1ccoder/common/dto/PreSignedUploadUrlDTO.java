package com.b4s1ccoder.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class PreSignedUploadUrlDTO {
  @NotBlank(message = "uploadUrl cannot be blank")
  private String uploadUrl;
}
