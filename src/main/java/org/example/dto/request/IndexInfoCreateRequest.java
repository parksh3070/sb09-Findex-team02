package org.example.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.entity.type.SourceType;

public record IndexInfoCreateRequest(
        String categoryName,
        String indexName,
        Integer component,
        LocalDate baseDate,
        BigDecimal baseIndex,
        SourceType sourceType,
        Boolean favorite
) {
}