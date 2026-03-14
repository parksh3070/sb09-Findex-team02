package org.example.repository;

import java.util.Optional;
import org.example.entity.IntegrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, Long> {
    //지수 정보 ID에 연결된 자동연동 설정을 조회하는 메서드
    Optional<IntegrationConfig> findByIndexInfoId(Long indexInfoId);
}