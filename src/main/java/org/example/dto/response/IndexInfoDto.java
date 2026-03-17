package org.example.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.example.entity.IndexInfo;
import org.example.entity.type.SourceType;

@Getter
@Builder
public class IndexInfoDto {

    private Long id;
    private String categoryName;
    private String indexName;
    private Integer component;
    private LocalDate baseDate;
    private BigDecimal baseIndex;
    private SourceType sourceType;
    private Boolean favorite;

    public static IndexInfoDto from(IndexInfo indexInfo) {
        return IndexInfoDto.builder()
                .id(indexInfo.getId())
                .categoryName(indexInfo.getCategoryName())
                .indexName(indexInfo.getIndexName())
                .component(indexInfo.getComponent())
                .baseDate(indexInfo.getBaseDate())
                .baseIndex(indexInfo.getBaseIndex())
                .sourceType(indexInfo.getSourceType())
                .favorite(indexInfo.getFavorite())
                .build();
    }
}