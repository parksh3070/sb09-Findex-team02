package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record CsvExportRequest(
    @Schema(description = "지수 정보 ID")
    Long indexInfoId,
    @Schema(description = "시작 일자")
    LocalDate startDate,
    @Schema(description = "종료 일자")
    LocalDate endDate,
    @Schema(description = "정렬 필드")
    String sortField,
    @Schema(description = "정렬 방향")
    String sortDirection
) {

}
