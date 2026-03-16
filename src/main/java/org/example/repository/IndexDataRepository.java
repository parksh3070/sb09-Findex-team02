package org.example.repository;

import java.time.Instant;
import java.util.List;
import org.example.entity.IndexData;
import org.example.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

  Optional<IndexData> findByIndexInfoAndBaseDate(
      IndexInfo indexId,
      Instant baseDate
  );

  List<IndexData> findByIndexInfo_IdAndBaseDateBetween(
      Long indexId,
      Instant startDate,
      Instant endDate
  );
}