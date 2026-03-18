package org.example.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.entity.IndexData;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import static org.example.entity.QIndexData.indexData;
import static org.example.entity.QIndexInfo.indexInfo;

@Repository
@RequiredArgsConstructor
public class IndexQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  public List<IndexData> findDataByDatesAndIndexName(List<LocalDate> baseDates, String indexName){
    return jpaQueryFactory
        .selectFrom(indexData)
        .join(indexData.indexInfo, indexInfo).fetchJoin()
        .where(
            indexData.baseDate.in(baseDates),
            indexNameEq(indexName)
        )
        .fetch();
  }

  public BooleanExpression indexNameEq(String indexName){
    if(!StringUtils.hasText(indexName)){
      return null;
    }
    return indexInfo.indexName.eq(indexName);
  }
}
