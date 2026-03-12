package org.example.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "지수 성과 정보 DTO")
public record RankedIndexPerformanceDto(
    @Schema(description = "성과")
     IndexPerformanceDto performance,
    @Schema(description = "랭크")
    Integer rank
) {
  public record IndexPerformanceDto(
      @Schema(description = "지수 정보 ID")
      Long indexInfoId,
      @Schema(description = "지수분류명")
      String indexClassification,
      @Schema(description = "지수명")
      String indexName,
      @Schema(description = "대비")
      BigDecimal versus,
      @Schema(description = "등락률")
      BigDecimal fluctuationRate,
      @Schema(description = "현재 가격")
      BigDecimal currentPrice,
      @Schema(description = "이전 가격")
      BigDecimal beforePrice
  ) {}

}
