package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

    boolean existsByCategoryNameAndIndexName(String categoryName, String indexName);

    Optional<IndexInfo> findByCategoryNameAndIndexName(String categoryName, String indexName);

    @Query("SELECT i.id FROM IndexInfo i WHERE i.favorite = true")
    List<Long> findFavoriteIndexIds();
}