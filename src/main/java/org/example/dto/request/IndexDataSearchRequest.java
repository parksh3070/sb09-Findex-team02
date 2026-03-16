package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "지수 데이터 목록 조회 요청 데이터")
public record IndexDataSearchRequest(
    @Schema(description = "지수 정보 ID")
    Long indexId,
    @Schema(description = "시작 일자")
    LocalDate startDate,
    @Schema(description = "종료 일자")
    LocalDate endDate,
    @Schema(description = "이전 페이지 마지막 요소 ID")
    Long idAfter,
    @Schema(description = "커서 (다음 페이지 시작점)")
    String cursor,
    @Schema(description = "정렬 필드")
    String sortField,
    @Schema(description = "정렬 방향 (asc, desc)")
    String sortDirection,
    @Schema(description = "페이지 크기")
    Integer size

) {

}
