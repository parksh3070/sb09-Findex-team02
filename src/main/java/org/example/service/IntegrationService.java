package org.example.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.IndexApiClient;
import org.example.dto.data.SyncJobDto;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

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
  private final IndexInfoService indexInfoService;

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
    LocalDate baseDate = LocalDate.parse(baseDateStr, YYYYMMDD);
    String startDateForApiStr = baseDate.minusDays(1).format(YYYYMMDD);
    String endDateForApiStr = baseDate.plusDays(1).format(YYYYMMDD);
    List<Item> fetchedItems = fetchAllItems(startDateForApiStr, endDateForApiStr);
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
      IndexInfo existing = indexInfoMap.get(item.indexName());
      if (existing != null) {
        existing.updateFromApi(toIndexInfoUpdateRequest(item));
        logList.add(IntegrationLog.createSuccess(JobType.INDEX_INFO, existing, LocalDate.now(), worker));
      } else {
        IndexInfoCreateRequest createReq = toIndexInfoCreateRequest(item);
        IndexInfo newIndex = new IndexInfo(createReq.indexName(), createReq.indexName(), SourceType.OPEN_API);
        newIndex.setIndexDetails(createReq.basePointInTime(), createReq.baseIndex(), createReq.employedItemsCount());

        newIndexInfoList.add(newIndex);
        autoSyncConfigList.add(new AutoSyncConfig(newIndex));
        logList.add(IntegrationLog.createSuccess(JobType.INDEX_INFO, newIndex, LocalDate.now(), worker));
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
    String endDateStr = endDate.plusDays(1).format(YYYYMMDD);

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

    Map<Long, AutoSyncConfig> configMap = new HashMap<>();
    for (IndexInfo indexInfo : targetIndexInfos) {
      autoSyncConfigRepository.findByIndexInfoId(indexInfo.getId())
          .ifPresent(config -> configMap.put(indexInfo.getId(), config));
    }

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
      logList.add(processIndexDataItem(item, indexInfo, worker, existingDataMap, indexDataList));

      // 연동 후 자동 연동 설정에 최근 연동 날짜 갱신
      AutoSyncConfig config = configMap.get(indexInfo.getId());
      if (config != null) {
        config.updateLastSyncAt(LocalDate.now());
      }
    }
    indexDataRepository.saveAll(indexDataList);
    integrationLogRepository.saveAll(logList);

    log.info("[지수 데이터 연동] 처리 완료. 총 {}건 (기간={} ~ {})",
        indexDataList.size(), startDateStr, endDateStr);
    return logList.stream().map(syncJobMapper::toDto).toList();
  }

  private IntegrationLog processIndexDataItem(Item item, IndexInfo indexInfo, String worker
      , Map<String, Map<LocalDate, IndexData>> existingDataMap,  List<IndexData> outputIndexDataList){
    LocalDate dataDate = parseLocalDate(item.dataBaseDate());

    try {
      IndexData existing = Optional.ofNullable(existingDataMap.get(item.indexName()))
          .map(dateMap -> dateMap.get(dataDate))
          .orElse(null);

      if (existing != null) {
        indexDataService.update(existing.getId(), dataDate,toIndexDataUpdateRequest(item));
        log.info("[지수 데이터 수정 성공] 이름={}, 날짜={}", item.indexName(), dataDate);
      } else {
        IndexData newIndexData = getIndexData(item, indexInfo, dataDate);
        outputIndexDataList.add(newIndexData);
        log.info("[지수 데이터 등록 성공] 이름={}, 날짜={}", item.indexName(), dataDate);
      }
      return IntegrationLog.createSuccess(JobType.INDEX_DATA, indexInfo, LocalDate.now(), worker);

    } catch (Exception e) {
      log.error("[지수 데이터 연동 에러] indexName={}, date={}, error={}",
          item.indexName(), dataDate, e.getMessage());
      return IntegrationLog.createFailed(JobType.INDEX_DATA, indexInfo, LocalDate.now(), worker);
    }
  }
  private IndexDataUpdateRequest toIndexDataUpdateRequest (Item item){
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
//연동 작업 목록 조회
    @Transactional(readOnly = true)
    public CursorPageResponseAutoSyncConfigDto<SyncJobDto> getSyncJobs (SyncJobSearchRequest request)
    {
      int size = request.size();
      Sort sort = buildSort(request);
      Specification<IntegrationLog> spec = SyncJobSpec.of(request);

      if (request.idAfter() != null) {
        IntegrationLog cursorLog = integrationLogRepository.findById(request.idAfter())
            .orElse(null);

        if (cursorLog != null) {
          spec = spec.and((root, query, cb) -> {
            String sortField = "targetDate".equalsIgnoreCase(request.sortField()) ? "targetDate" : "workedAt";
            Sort.Direction direction = "asc".equalsIgnoreCase(request.sortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;

            LocalDate cursorDate = "targetDate".equals(sortField) ? cursorLog.getTargetDate() : cursorLog.getWorkedAt();
            Long cursorId = cursorLog.getId();

            if (direction == Sort.Direction.ASC) {
              return cb.or(
                  cb.greaterThan(root.get(sortField), cursorDate),
                  cb.and(
                      cb.equal(root.get(sortField), cursorDate),
                      cb.greaterThan(root.get("id"), cursorId)
                  )
              );
            } else { // DESC
              return cb.or(
                  cb.lessThan(root.get(sortField), cursorDate),
                  cb.and(
                      cb.equal(root.get(sortField), cursorDate),
                      cb.lessThan(root.get("id"), cursorId)
                  )
              );
            }
          });
        }
      }

      List<IntegrationLog> fetched = integrationLogRepository.findAll(
          spec, PageRequest.of(0, size + 1, sort)
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
    private Sort buildSort (SyncJobSearchRequest request){
      Sort.Direction direction = "asc".equalsIgnoreCase(request.sortDirection())
          ? Sort.Direction.ASC : Sort.Direction.DESC;
      String field = "targetDate".equalsIgnoreCase(request.sortField())
          ? "targetDate" : "workedAt";
      return Sort.by(direction, field).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    //API 응답에서 Item 리스트 추출
    private List<Item> extractItems (OpenApiStockResponseDto response){
      return Optional.ofNullable(response)
          .map(OpenApiStockResponseDto::response)
          .map(OpenApiStockResponseDto.Response::body)
          .map(OpenApiStockResponseDto.Body::items)
          .map(OpenApiStockResponseDto.Items::item)
          .orElse(Collections.emptyList());
    }
    //기존 지수 데이터를 (indexName → (date → IndexData)) 구조의 Map으로 변환
    private Map<String, Map<LocalDate, IndexData>> buildExistingDataMap (
        List < IndexInfo > indexInfos, LocalDate startDate, LocalDate endDate){

      return indexInfos.stream().collect(Collectors.toMap(
          IndexInfo::getIndexName,
          indexInfo -> indexDataRepository
              .findByIndexInfo_IdAndBaseDateBetween(indexInfo.getId(), startDate, endDate)
              .stream()
              .collect(Collectors.toMap(
                  IndexData::getBaseDate,
                  d -> d,
                  (existing, replacement) -> existing
              )),
          (existing, replacement) -> existing
      ));
    }
    //item 데이터 toIndexInfoUpdateRequest로 변환
    private IndexInfoUpdateRequest toIndexInfoUpdateRequest (Item item){
      return new IndexInfoUpdateRequest(
          item.componentCount(),
          parseLocalDate(item.infoBaseDate()),
          item.baseIndex(),
          null
      );
    }
    //item 데이터 toIndexInfoCreateRequest로 변환
    private IndexInfoCreateRequest toIndexInfoCreateRequest (Item item){
      return new IndexInfoCreateRequest(
          item.categoryName(),
          item.indexName(),
          item.componentCount(),
          parseLocalDate(item.infoBaseDate()),
          item.baseIndex(),
          SourceType.OPEN_API,
          false
      );
    }


    //날짜(문자열 -> LocalDate로 변환)
    private LocalDate parseLocalDate (String dateStr){

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
  LocalDate endDate = LocalDate.now();
  LocalDate startDate = endDate.minusMonths(1);

  String startDateStr = startDate.format(YYYYMMDD);
  String endDateStr = endDate.format(YYYYMMDD);

  OpenApiStockResponseDto probeResponse = indexApiClient.getIndexData(
      serviceKey, 1, 1, startDateStr, endDateStr, "json"
  );

  List<Item> items = extractItems(probeResponse);
  if (!items.isEmpty()) {
    String baseDateStr = items.get(0).dataBaseDate();
    log.info("[기준일 결정] 최근 거래일={}", baseDateStr);
    return baseDateStr;
  }

  log.warn("[기준일 fallback] 최근 1개월 내 거래일 없음, 오늘 날짜 사용");
  return endDateStr;
}

    //페이지네이션으로 전체 데이터 조회
    private List<Item> fetchAllItems (String baseDateStr, String endDateStr){
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
      log.info("[지수 정보 연동] 총 {}건 조회 완료 (기간={} ~ {})", allItems.size(), baseDateStr, endDateStr);
      return allItems;
    }

    // IndexData 초기화
    private static @NonNull IndexData getIndexData (Item item, IndexInfo indexInfo,
        LocalDate dataDate){
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

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<SyncJobDto> autoSyncIndexData(List<IndexInfo> targetList, List<AutoSyncConfig> configList, LocalDate minLastSyncDate){

    String startDateStr = minLastSyncDate.format(YYYYMMDD);
    String endDateStr = LocalDate.now().plusDays(1).format(YYYYMMDD);

    if (targetList.isEmpty()) {
      log.warn("[지수 데이터 자동 연동] 대상 지수 없음.");
      return Collections.emptyList();
    }

    Map<String, IndexInfo> indexInfoMap = targetList.stream()
        .collect(Collectors.toMap(IndexInfo::getIndexName, i -> i, (existing, replacement) -> existing));

    Map<String, AutoSyncConfig> autoConfigMap = configList.stream()
        .collect(Collectors.toMap(
            config -> config.getIndexInfo().getIndexName(),
            config -> config,
            (existing, replacement) -> existing
        ));

    List<Item> fetchedItems = fetchAllItems(startDateStr, endDateStr);

    if (fetchedItems.isEmpty()) {
      log.warn("[지수 데이터 연동] API 응답 데이터 없음. 기간={} ~ {}", startDateStr, endDateStr);
      return Collections.emptyList();
    }

    List<Item> filteredItems = fetchedItems.stream()
        .filter(item -> {
          AutoSyncConfig config = autoConfigMap.get(item.indexName());
          if (config == null) return false;
          LocalDate itemDate = parseLocalDate(item.dataBaseDate());
          return itemDate != null && itemDate.isAfter(config.getLastSyncAt());
        })
        .toList();

    List<IndexData> indexDataList = new ArrayList<>();
    List<IntegrationLog> logList = new ArrayList<>();

    for (Item item : filteredItems) {
      IndexInfo indexInfo = indexInfoMap.get(item.indexName());
      logList.add(autoProcessIndexDataItem(item, indexInfo, indexDataList));
    }
    indexDataRepository.saveAll(indexDataList);
    integrationLogRepository.saveAll(logList);

    log.info("[지수 데이터 자동 연동] 처리 완료. 총 {}건 (기간={} ~ {})",
        indexDataList.size(), startDateStr, endDateStr);
    return logList.stream().map(syncJobMapper::toDto).toList();
  }

  private IntegrationLog autoProcessIndexDataItem(Item item, IndexInfo indexInfo, List<IndexData> outputIndexDataList){
    LocalDate dataDate = parseLocalDate(item.dataBaseDate());
    try {
      IndexData newIndexData = getIndexData(item, indexInfo, dataDate);
      outputIndexDataList.add(newIndexData);
      log.info("[지수 데이터 자동 등록 성공] 이름={}, 날짜={}", item.indexName(), dataDate);
      return IntegrationLog.createSuccess(JobType.INDEX_DATA, indexInfo, LocalDate.now(), "system");

    } catch (Exception e) {
      log.error("[지수 데이터 자동 연동 에러] indexName={}, date={}, error={}",
          item.indexName(), dataDate, e.getMessage());
      return IntegrationLog.createFailed(JobType.INDEX_DATA, indexInfo, LocalDate.now(), "system");
    }
  }
}
