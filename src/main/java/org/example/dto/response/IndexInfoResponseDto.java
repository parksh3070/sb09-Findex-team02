package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.entity.IndexInfo;
import org.example.entity.type.SourceType;

public record IndexInfoResponseDto(
        Long id,
        String categoryName,
        String indexName,
        Integer component,
        LocalDate baseDate,
        BigDecimal baseIndex,
        SourceType sourceType,
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