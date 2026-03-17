package org.example.scheduler.task;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.data.SyncJobDto;
import org.example.entity.AutoSyncConfig;
import org.example.entity.IndexInfo;
import org.example.entity.IntegrationLog;
import org.example.repository.AutoSyncConfigRepository;
import org.example.scheduler.BatchTask;
import org.example.service.IntegrationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoSyncTask implements BatchTask {

  private AutoSyncConfigRepository autoSyncConfigRepository;
  private IntegrationService integrationService;

  @Override
  public void execute() {
    List<AutoSyncConfig> configList = autoSyncConfigRepository.findAllEnabledWithIndexInfo();
    List<IndexInfo> targetList = configList.stream()
            .map(AutoSyncConfig::getIndexInfo)
                .toList();

    LocalDate minLastSync = configList.stream().
        map(AutoSyncConfig::getLastSyncAt)
        .filter(Objects::nonNull)
        .min(LocalDate::compareTo)
        .orElse(LocalDate.now());

    LocalDate startSyncTime = LocalDate.now();

    log.info("[지수 데이터 자동 연동 시작] 날짜={}", startSyncTime );

    List<SyncJobDto> integrationLogList = integrationService.autoSyncIndexData(targetList, configList, minLastSync);

    log.info("[지수 데이터 자동 연동 성공] 날짜={}, 개수={}", startSyncTime,  integrationLogList.size());
  }
}
