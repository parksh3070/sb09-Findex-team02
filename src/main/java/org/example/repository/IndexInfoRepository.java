package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>, IndexInfoRepositoryCustom {

    boolean existsByCategoryNameAndIndexName(String categoryName, String indexName);

    Optional<IndexInfo> findByCategoryNameAndIndexName(String categoryName, String indexName);

    @Query("SELECT i.id "
            + "FROM IndexInfo i "
            + "WHERE i.favorite = true")
    List<Long> findFavoriteIndexIds();

    @Query("SELECT i.id "
            + "FROM IndexInfo i")
    List<Long> findAllIds();

    @Query("SELECT i.id "
            + "FROM IndexInfo i "
            + "WHERE i.indexName = :indexName")
    List<Long> findIdsByIndexName(@Param("indexName") String indexName);
}