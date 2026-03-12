package org.example.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지수 정보 요약 DTO")
public record IndexInfoSummaryDto(

    @Schema(description = "지수 정보 ID", example = "1")
    Long id,

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName

) {
}