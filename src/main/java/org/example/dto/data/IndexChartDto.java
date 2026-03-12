package org.example.dto.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
@Schema(description = "지수 차트 데이터 DTO")
public record IndexChartDto(
    @Schema(description = "지수 정보 ID")
    Long indexInfoId,
    @Schema(description = "지수분류명")
    String indexClassification,
    @Schema(description = "지수명")
    String indexName,
    @Schema(description = "차트 기간 유형")
    String periodType,
    @Schema(description = "기본 차트 데이터")
    List<DataPoint> dataPoints,
    @Schema(description = "5일 이동평균선 데이터")
    List<DataPoint> ma5DataPoints,
    @Schema(description = "20일 이동평균선 데이터")
    List<DataPoint> ma20DataPoints
) {
  @Schema(description = "차트 데이터 포인트")
  public record DataPoint(
      @Schema(description = "데이터")
      Instant date,
      @Schema(description = "값")
      BigDecimal value
  ){}
}
