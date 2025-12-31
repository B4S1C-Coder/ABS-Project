package com.b4s1ccoder.common.dto;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import lombok.Builder;

@Data
@Builder
@Jacksonized
public class PartDTO {
  private Integer partNumber;
  private String eTag;
}
