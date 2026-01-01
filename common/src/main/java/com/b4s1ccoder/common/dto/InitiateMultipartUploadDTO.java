package com.b4s1ccoder.common.dto;

import jakarta.validation.constraints.NotBlank;
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
public class InitiateMultipartUploadDTO {
  // public InitiateMultipartUploadDTO() {
  // }

  // public InitiateMultipartUploadDTO(String filename, String title, String description) {
  //   this.filename = filename;
  //   this.title = title;
  //   this.description = description;
  // }

  @NotBlank(message = "Filename is required.")
  @Size(max = 255, message = "Filename should not be more than 255 characters long.")
  @Pattern(
    regexp = "^[a-zA-Z0-9._-]+$",
    message = "Filename can only contain alphanumeric characters, dots, underscores and hyphens"
  )
  private String filename;
  
  @Builder.Default
  @Size(max = 100, message = "Title must not exceed 100 characters")
  private String title = "Untitled";
  
  @Builder.Default
  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  private String description = "No Description Provided.";
}
