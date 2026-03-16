package org.example.dto.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;


@Schema(description = "지수 데이터 DTO")
public record IndexDataDto(
    @Schema(description = "지수 데이터 ID")
    Long id,
    @Schema(description = "지수 정보 ID")
    Long indexInfoId,
    @Schema(description = "기준 일자")
    LocalDate baseDate,
    @Schema(description = "정보가 입력된 출처")
    String sourceType,
    @Schema(description = "시가")
    BigDecimal marketPrice,
    @Schema(description = "종가")
    BigDecimal closingPrice,
    @Schema(description = "고가")
    BigDecimal highPrice,
    @Schema(description = "저가")
    BigDecimal lowPrice,
    @Schema(description = "대비")
    BigDecimal versus,
    @Schema(description = "등락률")
    BigDecimal fluctuationRate,
    @Schema(description = "거래량")
    Long tradingQuantity,
    @Schema(description = "거래대금")
    Long tradingPrice,
    @Schema(description = "상장 시가 총액")
    Long marketTotalAmount

) {

}
