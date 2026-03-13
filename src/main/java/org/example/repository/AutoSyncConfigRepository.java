package org.example.repository;

import java.util.List;
import org.example.entity.AutoSyncConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long> {

  @Query("SELECT c FROM AutoSyncConfig c " +
      "WHERE (:indexInfoId IS NULL OR c.indexInfo.id = :indexInfoId) " +
      "AND (:enabled IS NULL OR c.enabled = :enabled) " +
      "AND (:idAfter IS NULL OR c.id > :idAfter) ")
  List<AutoSyncConfig> findConfigsByCursor(
      Long indexInfoId,
      Boolean enabled,
      Long idAfter,
      Pageable pageable);

  @Query("SELECT COUNT(c) FROM AutoSyncConfig c " +
      "WHERE (:indexInfoId IS NULL OR c.indexInfo.id = :indexInfoId) " +
      "AND (:enabled IS NULL OR c.enabled = :enabled)")
  long countByFilters(Long indexInfoId, Boolean enabled);

  List<AutoSyncConfig> findAllByEnabled(Boolean enabled);
}
