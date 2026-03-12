package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "지수 데이터 수정 요청")
public record IndexDataUpdateRequest(
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
