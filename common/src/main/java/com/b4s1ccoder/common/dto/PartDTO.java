package com.b4s1ccoder.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.extern.jackson.Jacksonized;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
// @Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PartDTO {
  private Integer partNumber;
  private String eTag;
}
