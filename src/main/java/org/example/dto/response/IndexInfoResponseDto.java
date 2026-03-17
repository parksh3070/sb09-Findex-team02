package org.example.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.entity.IndexInfo;
import org.example.entity.type.SourceType;

@Schema(description = "지수 정보 응답")
public record IndexInfoResponseDto(

        @Schema(description = "지수 정보 ID", example = "1")
        Long id,

        @Schema(description = "지수 분류명", example = "KOSPI시리즈")
        String indexClassification,

        @Schema(description = "지수명", example = "IT 서비스")
        String indexName,

        @Schema(description = "채용 종목 수", example = "200")
        Integer employedItemsCount,

        @Schema(description = "기준 시점", example = "2000-01-01")
        LocalDate basePointInTime,

        @Schema(description = "기준 지수", example = "1000")
        BigDecimal baseIndex,

        @Schema(
                description = "소스 타입",
                example = "USER",
                allowableValues = {"USER", "OPEN_API"}
        )
        SourceType sourceType,

        @Schema(description = "즐겨찾기 여부", example = "false")
        Boolean favorite

) {
    public static IndexInfoResponseDto from(IndexInfo indexInfo) {
        return new IndexInfoResponseDto(
                indexInfo.getId(),
                indexInfo.getCategoryName(),
                indexInfo.getIndexName(),
                indexInfo.getComponent(),
                indexInfo.getBaseDate(),
                indexInfo.getBaseIndex(),
                indexInfo.getSourceType(),
                indexInfo.getFavorite()
        );
    }
}