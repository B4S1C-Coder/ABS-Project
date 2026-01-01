package com.b4s1ccoder.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
// import lombok.extern.jackson.Jacksonized;

@Data
@Builder
// @Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PreSignedUploadUrlDTO {
  @NotBlank(message = "uploadUrl cannot be blank")
  private String uploadUrl;
}
