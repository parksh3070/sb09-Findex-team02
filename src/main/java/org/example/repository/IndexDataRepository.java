package org.example.repository;


import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import org.example.entity.IndexData;
import org.example.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {


  Optional<IndexData> findByIndexInfoAndBaseDate(IndexInfo indexId, LocalDate baseDate);


  List<IndexData> findByIndexInfoAndBaseDateBetween(
      IndexInfo indexInfo,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable
  );


  @Query("SELECT i FROM IndexData i " +
      "WHERE i.indexInfo.favorite = true " + // 💡 IndexInfo의 favorite 필드가 true인 것만!
      "AND i.baseDate IN :dates " +
      "ORDER BY i.indexInfo.id ASC, i.baseDate DESC")
  List<IndexData> findAllBaseData(
      @Param("dates") List<LocalDate> dates
  );

  List<IndexData> findByIndexInfo_IdAndBaseDateBetween(
      Long indexId,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable
  );

  List<IndexData> findByIndexInfo_IdAndBaseDateBetweenAndIdGreaterThan(
      Long indexId,
      LocalDate startDate,
      LocalDate endDate,
      Long idAfter,
      Pageable pageable
  );

  //csv export
  List<IndexData> findByIndexInfo_IdAndBaseDateBetween(
      Long indexId,
      LocalDate startDate,
      LocalDate endDate,
      Sort sort
  );

}