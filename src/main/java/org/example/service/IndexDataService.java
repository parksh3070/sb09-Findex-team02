package org.example.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.response.FavoritePerformanceResponse;
import org.example.dto.response.RankedIndexPerformanceDto.IndexPerformanceDto;
import org.example.entity.IndexData;
import org.example.entity.IndexInfo;
import org.example.repository.IndexDataRepository;
import org.example.repository.IndexInfoRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository;

  @Transactional
  public Long create(IndexDataCreateRequest request) {

    // 지수 정보 조회
    IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수입니다."));

    LocalDate baseDate = request.baseDate();

    // 중복 체크 (indexInfo + baseDate)
    indexDataRepository
        .findByIndexInfoAndBaseDate(indexInfo, baseDate)
        .ifPresent(data -> {
          throw new IllegalArgumentException("이미 존재하는 지수 데이터입니다.");
        });

    // 엔티티 생성
    IndexData indexData = getIndexData(request, indexInfo, baseDate);

    // 저장
    indexDataRepository.save(indexData);

    return indexData.getId();
  }

  private static @NonNull IndexData getIndexData(IndexDataCreateRequest request,
      IndexInfo indexInfo, LocalDate baseDate) {
    IndexData indexData = new IndexData(
        indexInfo,
        baseDate,
        request.sourceType()
    );

    // 가격 정보
    indexData.setPrices(
        request.marketPrice(),
        request.closingPrice(),
        request.highPrice(),
        request.lowPrice()
    );

    // 등락 정보
    indexData.setFluctuationInfo(
        request.versus(),
        request.fluctuationRate()
    );

    // 시장 데이터
    indexData.setMarketData(
        request.tradingQuantity(),
        request.tradingPrice(),
        request.marketTotalAmount()
    );
    return indexData;
  }

  public List<FavoritePerformanceResponse> getFavoritePerformances(String periodType) {
    ZoneId zoneId = ZoneId.of("Asia/Seoul");
    LocalDate today = LocalDate.now(zoneId).atStartOfDay(zoneId).toLocalDate();
    LocalDate baseDate;

    List<Long> favoriteIndexIds = indexInfoRepository.findFavoriteIndexIds();

    if(favoriteIndexIds.isEmpty()) {
      return Collections.emptyList();
    }

    switch(periodType.toUpperCase()) {
      case "DAILY" :
      default:
        baseDate = today.minus(1, ChronoUnit.DAYS);
        break;
      case "WEEKLY" :
        baseDate = today.minus(7, ChronoUnit.DAYS);
        break;
      case "MONTHLY" :
        baseDate = today.minus(30,ChronoUnit.DAYS);
        break;
    }

    List<LocalDate> baseDates = List.of(today,baseDate);
    List<IndexData> dataList = indexDataRepository.findAllBaseData(baseDates);


    Map<Long, List<IndexData>> groupedData = dataList.stream()
        .collect(Collectors.groupingBy(data -> data.getIndexInfo().getId()));

    List<IndexPerformanceDto> performances = groupedData.entrySet().stream()
        .map(entry-> calculatePerformance(entry.getKey(), entry.getValue()))
        .toList();
    AtomicInteger rankCounter = new AtomicInteger(1);

    return dataList.stream()
        .collect(Collectors.groupingBy(data -> data.getIndexInfo().getId()))
        .entrySet().stream()
        .map(entry -> {
          Long indexId = entry.getKey();
          List<IndexData> indexData = entry.getValue();

          if(indexData.size() < 2) return null;

          IndexData current = indexData.get(0);
          IndexData before = indexData.get(1);

          BigDecimal versus = current.getClosingPrice().subtract(before.getClosingPrice());
          BigDecimal fluctuationRate = versus.divide(before.getClosingPrice(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

          return new FavoritePerformanceResponse(
              indexId,
              current.getIndexInfo().getCategoryName(),
              current.getIndexInfo().getIndexName(),
              versus,
              fluctuationRate,
              current.getClosingPrice(),
              before.getClosingPrice()
          );
        })
        .filter(Objects::nonNull)
        .sorted((a,b) -> b.fluctuationRate().compareTo(a.fluctuationRate()))
        .toList();
  }

  public IndexPerformanceDto calculatePerformance(Long indexId, List<IndexData> dataList){
    if(dataList == null || dataList.isEmpty()) {
      throw new IllegalArgumentException("해당 지수의 데이터가 존재하지 않습니다.");
    }

    IndexData todayData = dataList.get(0);
    BigDecimal todayClosePrice = todayData.getClosingPrice();
    BigDecimal yesterdayClosePrice = (dataList.size() > 1) ? dataList.get(1).getClosingPrice() : todayClosePrice;

    // 등락폭 계산 (오늘 종가 - 어제 종가)
    BigDecimal priceDiff = todayClosePrice.subtract(yesterdayClosePrice);


    // 등락률 계산 ((등락폭 / 어제 종가) * 100)
    BigDecimal fluctuationRate = (yesterdayClosePrice.compareTo(BigDecimal.ZERO) == 0)
        ? BigDecimal.ZERO // 참이면 등락률 0
        : priceDiff.divide(yesterdayClosePrice, 4, RoundingMode.HALF_UP) // 거짓이면 반올림 해서 소수점 4자리까지
            .multiply(BigDecimal.valueOf(100));

    String indexName = todayData.getIndexInfo().getIndexName();
    String indexClassification = todayData.getIndexInfo().getCategoryName();

    return new IndexPerformanceDto(
        indexId,
        indexClassification,
        indexName,
        priceDiff,
        fluctuationRate,
        todayClosePrice,
        yesterdayClosePrice
    );

  }
}