package com.b4s1ccoder.common.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class CompleteMultipartUploadDTO {
  @NotBlank(message = "Filename is required.")
  @Size(max = 255, message = "Filename should not be more than 255 characters long.")
  @Pattern(
    regexp = "^[a-zA-Z0-9._-]+$",
    message = "Filename can only contain alphanumeric characters, dots, underscores and hyphens"
  )
  private String filename;
  
  @NotBlank(message = "uploadId cannot be blank")
  private String uploadId;

  @NotBlank(message = "key cannot be blank")
  private String key;

  @NotEmpty(message = "parts must be provided")
  private List<PartDTO> parts;
}
