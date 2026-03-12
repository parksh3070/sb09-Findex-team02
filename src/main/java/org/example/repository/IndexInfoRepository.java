package org.example.repository;

import java.util.Optional;
import org.example.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

    boolean existsByCategoryNameAndIndexName(String categoryName, String indexName);

    Optional<IndexInfo> findByCategoryNameAndIndexName(String categoryName, String indexName);
}