package org.example.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.IndexApiClient;
import org.example.dto.data.SyncJobDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.dto.request.IndexInfoUpdateRequest;
import org.example.dto.request.SyncJobSearchRequest;
import org.example.dto.response.CursorPageResponseAutoSyncConfigDto;
import org.example.dto.response.OpenApiStockResponseDto;
import org.example.dto.response.OpenApiStockResponseDto.Item;
import org.example.entity.AutoSyncConfig;
import org.example.entity.IndexData;
import org.example.entity.IndexInfo;
import org.example.entity.IntegrationLog;
import org.example.entity.type.JobType;
import org.example.entity.type.SourceType;
import org.example.mapper.SyncJobMapper;
import org.example.repository.AutoSyncConfigRepository;
import org.example.repository.IndexDataRepository;
import org.example.repository.IndexInfoRepository;
import org.example.repository.IntegrationLogRepository;
import org.example.repository.SyncJobSpec;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

  private final IntegrationLogRepository integrationLogRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataRepository indexDataRepository;
  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final IndexApiClient indexApiClient;
  private final IndexDataService indexDataService;
  private final SyncJobMapper syncJobMapper;

  @Value("${openapi.service-key}")
  private String serviceKey;
  private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final int PAGE_SIZE = 500;
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  //자수 정보 연동
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<SyncJobDto> syncIndexInfos(String worker) {
    String baseDateStr = resolveBaseDate();
    List<Item> fetchedItems = fetchAllItems(baseDateStr, baseDateStr);

    if (fetchedItems.isEmpty()) {
      log.warn("[지수 정보 연동] API 응답 데이터 없음. 기준일={}", baseDateStr);
      return Collections.emptyList();
    }

    Map<String, IndexInfo> indexInfoMap = indexInfoRepository.findAll().stream()
        .collect(Collectors.toMap(IndexInfo::getIndexName, i -> i));

    List<IndexInfo> newIndexInfoList = new ArrayList<>();
    List<IntegrationLog> logList = new ArrayList<>();
    List<AutoSyncConfig> autoSyncConfigList = new ArrayList<>();

    for (Item item : fetchedItems) {
      IndexInfo indexInfo = indexInfoMap.get(item.indexName());
      try {
        if (indexInfo != null) {
          //indexInfoService.update(indexInfo.getId(), toIndexInfoUpdateRequest(item));
          logList.add(IntegrationLog.createSuccess(JobType.INDEX_INFO, indexInfo,
              LocalDate.now(), worker));
          log.info("[지수 정보 수정 성공] 이름={}", item.indexName());
        } else {
          IndexInfoCreateRequest infoCreateRequest = toIndexInfoCreateRequest(item);

          IndexInfo newIndex = new IndexInfo(infoCreateRequest.indexClassification(), infoCreateRequest.indexName(), SourceType.OPEN_API);
          newIndex.setIndexDetails(infoCreateRequest.basePointInTime(),
              BigDecimal.valueOf(infoCreateRequest.baseIndex())
              , infoCreateRequest.employedItemsCount());
          newIndexInfoList.add(newIndex);
          autoSyncConfigList.add(new AutoSyncConfig(newIndex));

          logList.add(IntegrationLog.createSuccess(JobType.INDEX_INFO, newIndex, LocalDate.now(), worker));
          log.info("[지수 정보 등록 성공] 이름={}", item.indexName());
        }
      } catch (Exception e) {
        log.error("[연동 에러] indexName={}, error={}", item.indexName(), e.getMessage());
        logList.add(IntegrationLog.createFailed(JobType.INDEX_INFO, indexInfo, LocalDate.now(), worker));
      }
    }

    indexInfoRepository.saveAll(newIndexInfoList);
    integrationLogRepository.saveAll(logList);
    autoSyncConfigRepository.saveAll(autoSyncConfigList);

    return logList.stream().map(syncJobMapper::toDto).toList();
  }

//지수 데이터 연동
@Transactional
public List<SyncJobDto> syncIndexData(String worker, LocalDate startDate, LocalDate endDate,
    List<Long> indexInfoIdList) {

  String startDateStr = startDate.format(YYYYMMDD);
  String endDateStr = endDate.format(YYYYMMDD);

  List<IndexInfo> targetIndexInfos = (indexInfoIdList != null && !indexInfoIdList.isEmpty())
      ? indexInfoRepository.findAllById(indexInfoIdList)
      : indexInfoRepository.findAll();

  if (targetIndexInfos.isEmpty()) {
    log.warn("[지수 데이터 연동] 대상 지수 없음. indexInfoId={}", targetIndexInfos);
    return Collections.emptyList();
  }

  Set<String> targetNames = targetIndexInfos.stream()
      .map(IndexInfo::getIndexName)
      .collect(Collectors.toSet());

  Map<String, IndexInfo> indexInfoMap = targetIndexInfos.stream()
      .collect(Collectors.toMap(IndexInfo::getIndexName, i -> i));

  List<Item> fetchedItems = fetchAllItems(startDateStr, endDateStr);

  if (fetchedItems.isEmpty()) {
    log.warn("[지수 데이터 연동] API 응답 데이터 없음. 기간={} ~ {}", startDateStr, endDateStr);
    return Collections.emptyList();
  }

  List<Item> filteredItems = fetchedItems.stream()
      .filter(item -> targetNames.contains(item.indexName()))
      .toList();

  Map<String, Map<LocalDate, IndexData>> existingDataMap =
      buildExistingDataMap(targetIndexInfos, startDate, endDate);

  List<IndexData> indexDataList = new ArrayList<>();
  List<IntegrationLog> logList = new ArrayList<>();

  for (Item item : filteredItems) {
    IndexInfo indexInfo = indexInfoMap.get(item.indexName());
    LocalDate dataDate = parseLocalDate(item.dataBaseDate());

    try {
      IndexData existing = Optional.ofNullable(existingDataMap.get(item.indexName()))
          .map(dateMap -> dateMap.get(dataDate))
          .orElse(null);

      if (existing != null) {
//        indexDataService.update(existing.getId(), toIndexDataUpdateRequest(item));
        log.info("[지수 데이터 수정 성공] 이름={}, 날짜={}", item.indexName(), dataDate);
      } else {
        IndexData newIndexData = getIndexData(item, indexInfo, dataDate);
        indexDataList.add(newIndexData);
        log.info("[지수 데이터 등록 성공] 이름={}, 날짜={}", item.indexName(), dataDate);
      }
      logList.add(IntegrationLog.createSuccess(JobType.INDEX_DATA, indexInfo, LocalDate.now(), worker));

    } catch (Exception e) {
      log.error("[지수 데이터 연동 에러] indexName={}, date={}, error={}",
          item.indexName(), dataDate, e.getMessage());
      logList.add(IntegrationLog.createFailed(JobType.INDEX_DATA, indexInfo, LocalDate.now(), worker));
    }
  }

//연동 작업 목록 조회
@Transactional(readOnly = true)
public CursorPageResponseAutoSyncConfigDto<SyncJobDto> getSyncJobs(SyncJobSearchRequest request) {
  int size = request.size();

  List<IntegrationLog> fetched = integrationLogRepository.findAll(
      SyncJobSpec.of(request), PageRequest.of(0, size + 1, buildSort(request))
  ).getContent();

  long totalElements = integrationLogRepository.count(SyncJobSpec.of(request));
  boolean hasNext = fetched.size() > size;
  List<IntegrationLog> pageItems = hasNext ? fetched.subList(0, size) : fetched;

  List<SyncJobDto> content = pageItems.stream()
      .map(syncJobMapper::toDto)
      .toList();

  Long nextIdAfter = null;
  String nextCursor = null;
  if (hasNext && !pageItems.isEmpty()) {
    Long lastId = pageItems.get(pageItems.size() - 1).getId();
    nextIdAfter = lastId;
    nextCursor = String.valueOf(lastId);
  }
  return new CursorPageResponseAutoSyncConfigDto<>(
      content, nextCursor, nextIdAfter, size, totalElements, hasNext
  );
}

//정렬
  private Sort buildSort(SyncJobSearchRequest request) {
    Sort.Direction direction = "asc".equalsIgnoreCase(request.sortDirection())
        ? Sort.Direction.ASC : Sort.Direction.DESC;
    String field = "targetDate".equalsIgnoreCase(request.sortField())
        ? "targetDate" : "workedAt";
    return Sort.by(direction, field).and(Sort.by(Sort.Direction.DESC, "id"));
  }

  //API 응답에서 Item 리스트 추출
  private List<Item> extractItems(OpenApiStockResponseDto response) {
    return Optional.ofNullable(response)
        .map(OpenApiStockResponseDto::response)
        .map(OpenApiStockResponseDto.Response::body)
        .map(OpenApiStockResponseDto.Body::items)
        .map(OpenApiStockResponseDto.Items::item)
        .orElse(Collections.emptyList());
  }
  //기존 지수 데이터를 (indexName → (date → IndexData)) 구조의 Map으로 변환
  private Map<String, Map<LocalDate, IndexData>> buildExistingDataMap(
      List<IndexInfo> indexInfos, LocalDate startDate, LocalDate endDate) {

    return indexInfos.stream().collect(Collectors.toMap(
        IndexInfo::getIndexName,
        indexInfo -> indexDataRepository
            .findByIndexInfoAndBaseDateBetween(indexInfo, startDate, endDate)
            .stream()
            .collect(Collectors.toMap(
                IndexData::getBaseDate,
                d -> d
            ))
    ));
  }
  //item 데이터 toIndexInfoUpdateRequest로 변환
  private IndexInfoUpdateRequest toIndexInfoUpdateRequest(Item item) {
    return new IndexInfoUpdateRequest(
        item.componentCount(),
        parseLocalDate(item.infoBaseDate()),
        item.baseIndex().doubleValue(),
        null
    );
  }
  //item 데이터 toIndexInfoCreateRequest로 변환
  private IndexInfoCreateRequest toIndexInfoCreateRequest(Item item) {
    return new IndexInfoCreateRequest(
        item.categoryName(),
        item.indexName(),
        item.componentCount(),
        parseLocalDate(item.infoBaseDate()),
        item.baseIndex().doubleValue(),
        false
    );
  }

  private IndexDataCreateRequest toIndexDataCreateRequest(Item item, IndexInfo indexInfo) {
    LocalDate date = parseLocalDate(item.dataBaseDate());
    return new IndexDataCreateRequest(
        indexInfo.getId(),
        date,
        item.openPrice(),
        item.closePrice(),
        item.highPrice(),
        item.lowPrice(),
        item.priceDiff(),
        item.fluctuationRate(),
        item.tradeVolume(),
        item.tradeAmount(),
        item.marketCap(),
        SourceType.OPEN_API
    );
  }

  private IndexDataUpdateRequest toIndexDataUpdateRequest(Item item) {
    return new IndexDataUpdateRequest(
        item.openPrice(),
        item.closePrice(),
        item.highPrice(),
        item.lowPrice(),
        item.priceDiff(),
        item.fluctuationRate(),
        item.tradeVolume(),
        item.tradeAmount(),
        item.marketCap()
    );
  }


  //날짜(문자열 -> LocalDate로 변환)
  private LocalDate parseLocalDate(String dateStr) {

    if (dateStr == null || dateStr.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(dateStr, YYYYMMDD);
    } catch (DateTimeParseException e) {
      log.warn("날짜 파싱 실패: {}", dateStr);
      return null;
    }
  }

//기준일 결정
  private String resolveBaseDate() {
    LocalDate today = LocalDate.now();
    String todayStr = today.format(YYYYMMDD);

    OpenApiStockResponseDto probeResponse = indexApiClient.getIndexData(
        serviceKey, 1, 1, todayStr, todayStr, "json"
    );

    if (!extractItems(probeResponse).isEmpty()) {
      return todayStr;
    }

    String yesterdayStr = today.minusDays(1).format(YYYYMMDD);
    log.info("[기준일 fallback] 오늘({}) 데이터 없음 → 전일({}) 사용", todayStr, yesterdayStr);
    return yesterdayStr;
  }

  //페이지네이션으로 전체 데이터 조회
  private List<Item> fetchAllItems(String baseDateStr, String endDateStr) {
    List<Item> allItems = new ArrayList<>();
    int pageNo = 1;

    while (true) {
      OpenApiStockResponseDto response = indexApiClient.getIndexData(
          serviceKey, PAGE_SIZE, pageNo, baseDateStr, endDateStr, "json"
      );
      List<Item> items = extractItems(response);
      allItems.addAll(items);

      if (items.isEmpty() || items.size() < PAGE_SIZE) {
        break;
      }
      pageNo++;
    }
    log.info("[지수 정보 연동] 총 {}건 조회 완료 (기준일={})", allItems.size(), baseDateStr);
    return allItems;
  }

  // IndexData 초기화
  private static @NonNull IndexData getIndexData(Item item, IndexInfo indexInfo,
      LocalDate dataDate) {
    IndexData newIndexData = new IndexData(indexInfo, dataDate, SourceType.OPEN_API);
    newIndexData.setPrices(
        item.openPrice(),
        item.closePrice(),
        item.highPrice(),
        item.lowPrice()
    );
    // 등락 정보
    newIndexData.setFluctuationInfo(
        item.priceDiff(),
        item.fluctuationRate()
    );
    // 시장 데이터
    newIndexData.setMarketData(
        item.tradeVolume(),
        item.tradeAmount(),
        item.marketCap()
    );
    return newIndexData;
  }

}