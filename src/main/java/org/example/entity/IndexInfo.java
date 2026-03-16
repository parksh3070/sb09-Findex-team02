package org.example.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.entity.base.BaseEntity;
import org.example.entity.type.SourceType;

import java.math.BigDecimal;


@Entity
@Table(name = "index_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexInfo extends BaseEntity {

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "index_name", nullable = false)
    private String indexName;

    @Column(name = "component_count", nullable = false)
    //JPA에서 NULL을 처리하기위해 integer사용
    private Integer component;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "base_index", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "favorite", nullable = false)
    private Boolean favorite;

    @Setter(AccessLevel.PROTECTED)
    @OneToMany(mappedBy = "indexInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexData> indexDataList = new ArrayList<>();


    public IndexInfo(String categoryName, String indexName, SourceType sourceType) {
        this.categoryName = categoryName;
        this.indexName = indexName;
        this.sourceType = sourceType;
        this.favorite = false;
    }

    // 생성자 파라미터가 너무 길어져서 분리
    // 생성자 호출하면서 같이 사용해야함

    public void setIndexDetails(LocalDate baseData, BigDecimal baseIndex, Integer component) {

        this.baseDate = baseData;
        this.baseIndex = baseIndex;
        this.component = component;
    }

    // 즐겨찾기 수정
    public void updateFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    // IndexData 추가 후 indexDataList에 추가
    public void addIndexData(IndexData data){
        this.indexDataList.add(data);
        if (data.getIndexInfo() != this) {
            // 예외처리 해야함
            // 임시
            throw new IllegalArgumentException();
        }
    }
}
