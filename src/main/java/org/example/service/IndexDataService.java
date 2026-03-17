package org.example.service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.example.dto.data.IndexDataDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataSearchRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.dto.response.FavoritePerformanceResponse;
import org.example.dto.response.RankedIndexPerformanceDto;
import org.example.dto.response.RankedIndexPerformanceDto.IndexPerformanceDto;
import org.example.entity.IndexData;
import org.example.entity.IndexInfo;
import org.example.mapper.IndexDataMapper;
import org.example.repository.IndexDataRepository;
import org.example.repository.IndexInfoRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataMapper indexDataMapper;

  //생성
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

  //조회
  public List<IndexDataDto> search(IndexDataSearchRequest request) {

    return indexDataRepository
        .findByIndexInfo_IdAndBaseDateBetween(
            request.indexId(),
            request.startDate(),
            request.endDate()
        )
        .stream()
        .map(indexDataMapper::toDto)
        .toList();
  }

  //업데이트
  @Transactional
  public Long update(Long indexId, Instant baseDate, IndexDataUpdateRequest request) {

    IndexInfo indexInfo = indexInfoRepository.findById(indexId)
        .orElseThrow(() -> new NoSuchElementException("Index not found"));

    IndexData indexData = indexDataRepository
        .findByIndexInfoAndBaseDate(indexInfo, baseDate)
        .orElseThrow(() -> new NoSuchElementException("Index data not found"));

    indexData.setPrices(
        request.marketPrice(),
        request.closingPrice(),
        request.highPrice(),
        request.lowPrice()
    );

    indexData.setFluctuationInfo(
        request.versus(),
        request.fluctuationRate()
    );

    indexData.setMarketData(
        request.tradingQuantity(),
        request.tradingPrice(),
        request.marketTotalAmount()
    );

    return indexData.getId();
  }

  //삭제
  @Transactional
  public void delete(Long indexId, Instant baseDate) {

    IndexInfo indexInfo = indexInfoRepository.findById(indexId)
        .orElseThrow(() -> new NoSuchElementException("Index not found"));

    IndexData indexData = indexDataRepository
        .findByIndexInfoAndBaseDate(indexInfo, baseDate)
        .orElseThrow(() -> new NoSuchElementException("Index data not found"));

    indexDataRepository.delete(indexData);
  }



  public List<FavoritePerformanceResponse> getFavoritePerformances(String periodType) {
    List<LocalDate> lateDates = indexDataRepository.findDistinctByBaseDate(PageRequest.of(0,2));
    if (lateDates == null || lateDates.size() < 2) {
      throw new NoSuchElementException("데이터가 충분하지 않습니다. 현재 DB 날짜 개수: {}");
    }

    LocalDate today = lateDates.get(0);
    LocalDate baseDate;
    List<Long> favoriteIndexIds = indexInfoRepository.findFavoriteIndexIds();

    if(favoriteIndexIds.isEmpty()) {
      return Collections.emptyList();
    }

    switch(periodType.toUpperCase()) {
      case "DAILY" :
      default:
        baseDate = lateDates.get(1);
        break;
      case "WEEKLY" :
        baseDate = today.minus(7, ChronoUnit.DAYS);
        break;
      case "MONTHLY" :
        baseDate = today.minus(30,ChronoUnit.DAYS);
        break;
    }

    List<LocalDate> baseDates = List.of(today,baseDate);
    List<IndexData> dataList = indexDataRepository.findAllBaseData(favoriteIndexIds,baseDates);


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

  public List<RankedIndexPerformanceDto> getPerformanceRanking(Long indexInfold, String categoryName, String periodType, Integer limit){
    List<LocalDate> lateDates = indexDataRepository.findDistinctByBaseDate(PageRequest.of(0,2));
    if (lateDates == null || lateDates.size() < 2) {
      throw new NoSuchElementException("데이터가 충분하지 않습니다. 현재 DB 날짜 개수: {}");
    }

    int rankLimit = (limit == null ) ? 10 : Math.min(limit, 10);

    LocalDate today = lateDates.get(0);
    LocalDate baseDate;

    switch(periodType.toUpperCase()) {
      case "DAILY" :
      default:
        baseDate = lateDates.get(1);
        break;
      case "WEEKLY" :
        baseDate = today.minus(7, ChronoUnit.DAYS);
        break;
      case "MONTHLY" :
        baseDate = today.minus(30,ChronoUnit.DAYS);
        break;
    }

    List<Long> rankingIndexIds;

    if (categoryName != null && !categoryName.isEmpty()) {
      rankingIndexIds = indexInfoRepository.findIdsByCategoryName(categoryName);
    } else {
      rankingIndexIds = indexInfoRepository.findAllIds();
    }

    if (rankingIndexIds.isEmpty()) {
      return Collections.emptyList();
    }

    List<LocalDate> baseDates = List.of(today,baseDate);
    List<IndexData> dataList = indexDataRepository.findAllBaseData(rankingIndexIds, baseDates);

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

          BigDecimal currentClosingPrice = current.getClosingPrice();
          BigDecimal beforeClosingPrice = before.getClosingPrice();
          BigDecimal versus = currentClosingPrice.subtract(beforeClosingPrice);
          BigDecimal fluctuationRate = BigDecimal.ZERO;
          if(beforeClosingPrice.compareTo(BigDecimal.ZERO) != 0) {
            fluctuationRate = versus.divide(beforeClosingPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
          }

          return new IndexPerformanceDto(
              indexId,
              current.getIndexInfo().getCategoryName(),
              current.getIndexInfo().getIndexName(),
              versus,
              fluctuationRate,
              currentClosingPrice,
              beforeClosingPrice
          );
        })
        .filter(Objects::nonNull)
        .sorted((a, b) -> b.fluctuationRate().compareTo(a.fluctuationRate()))
        .map(indexPerformanceDto -> {
          return new RankedIndexPerformanceDto(indexPerformanceDto, rankCounter.getAndIncrement());
        })
        .limit(rankLimit)
        .toList();
  }
}