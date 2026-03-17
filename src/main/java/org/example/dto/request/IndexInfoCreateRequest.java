package org.example.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.entity.type.SourceType;

@Getter
@NoArgsConstructor
public class IndexInfoCreateRequest {

    private String categoryName;
    private String indexName;
    private Integer component;
    private LocalDate baseDate;
    private BigDecimal baseIndex;
    private SourceType sourceType;
    private Boolean favorite;
}