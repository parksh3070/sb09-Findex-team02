package org.example.service;


import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexChartDto;
import org.example.dto.data.IndexDataDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataSearchRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.dto.response.CursorPageResponseIndexDataDto;
import org.example.dto.response.FavoritePerformanceResponse;
import org.example.dto.response.RankedIndexPerformanceDto;
import org.example.dto.response.RankedIndexPerformanceDto.IndexPerformanceDto;
import org.example.entity.IndexData;
import org.example.entity.IndexInfo;
import org.example.entity.type.SourceType;
import org.example.mapper.IndexDataMapper;
import org.example.repository.IndexDataRepository;
import org.example.repository.IndexInfoRepository;
import org.example.repository.IndexQueryRepository;
import org.jspecify.annotations.NonNull;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataService {

  private final IndexDataRepository indexDataRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataMapper indexDataMapper;
  private final IndexQueryRepository indexQueryRepository;

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
        SourceType.USER
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
  public CursorPageResponseIndexDataDto<IndexDataDto> search(IndexDataSearchRequest request) {

    int size = request.size() == null ? 10 : request.size();

    String sortField = request.sortField() == null ? "id" : request.sortField();

    Sort.Direction direction =
        request.sortDirection() != null && request.sortDirection().equalsIgnoreCase("desc")
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

    Pageable pageable = PageRequest.of(0, size + 1, Sort.by(direction, sortField)); // ⭐ hasNext 판단용 +1

    List<IndexData> result;

    if (request.idAfter() != null) {
      result = indexDataRepository.findByIndexInfo_IdAndBaseDateBetweenAndIdGreaterThan(
          request.indexId(),
          request.startDate(),
          request.endDate(),
          request.idAfter(),
          pageable
      );
    } else {
      result = indexDataRepository.findByIndexInfo_IdAndBaseDateBetween(
          request.indexId(),
          request.startDate(),
          request.endDate(),
          pageable
      );
    }

    boolean hasNext = result.size() > size;

    if (hasNext) {
      result = result.subList(0, size);
    }

    List<IndexDataDto> content = result.stream()
        .map(indexDataMapper::toDto)
        .toList();

    Long nextIdAfter = content.isEmpty()
        ? null
        : result.get(result.size() - 1).getId();

    String nextCursor = nextIdAfter == null ? null : String.valueOf(nextIdAfter);

    return new CursorPageResponseIndexDataDto<>(
        content,
        nextCursor,
        nextIdAfter,
        size,
        0L ,// totalElements (cursor 방식에서는 보통 안씀)
        hasNext
    );
  }
  //업데이트
  @Transactional
  public Long update(Long indexId, LocalDate baseDate, IndexDataUpdateRequest request) {

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
  public void delete(Long indexId, LocalDate baseDate) {

    IndexInfo indexInfo = indexInfoRepository.findById(indexId)
        .orElseThrow(() -> new NoSuchElementException("Index not found"));

    IndexData indexData = indexDataRepository
        .findByIndexInfoAndBaseDate(indexInfo, baseDate)
        .orElseThrow(() -> new NoSuchElementException("Index data not found"));

    indexDataRepository.delete(indexData);
  }

  //csv파일로 export
  public ByteArrayInputStream export(IndexDataSearchRequest request) {

    String sortField = request.sortField() == null ? "id" : request.sortField();

    Sort.Direction direction =
        request.sortDirection() != null && request.sortDirection().equalsIgnoreCase("desc")
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

    Sort sort = Sort.by(direction, sortField);

    List<IndexData> data = indexDataRepository
        .findByIndexInfo_IdAndBaseDateBetween(
            request.indexId(),
            request.startDate(),
            request.endDate(),
            sort
        );

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

    writer.println("\uFEFFid,indexId,baseDate,openPrice,closePrice,highPrice,lowPrice");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    for (IndexData i : data) {
      writer.printf("%d,%d,%s,%s,%s,%s,%s%n",
          i.getId(),
          i.getIndexInfo().getId(),
          i.getBaseDate().format(formatter),
          i.getMarketPrice(),
          i.getClosingPrice(),
          i.getHighPrice(),
          i.getLowPrice()
      );
    }

    writer.flush();

    return new ByteArrayInputStream(out.toByteArray());
  }


  @Transactional
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
      case "WEEKLY" :
        baseDate = today.minusWeeks(1);
        break;
      case "MONTHLY" :
        baseDate = today.minusMonths(1);
        break;
      case "DAILY" :
      default:
        baseDate = lateDates.get(1);
        break;
    }

    List<LocalDate> baseDates = List.of(today,baseDate);
    List<IndexData> dataList = indexDataRepository.findAllFavoriteBaseData(favoriteIndexIds,baseDates);


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
  @Transactional(readOnly = true)
  public List<RankedIndexPerformanceDto> getPerformanceRanking(Long indexInfold, String categoryName, String periodType, Integer limit){

    List<LocalDate> lateDates = indexDataRepository.findDistinctByBaseDate(PageRequest.of(0,2));
    if (lateDates == null || lateDates.size() < 2) {
      throw new NoSuchElementException("데이터가 충분하지 않습니다. 현재 DB 날짜 개수: {}");
    }

    int rankLimit = (limit == null ) ? 10 : Math.min(limit, 10);

    LocalDate today = lateDates.get(0);
    LocalDate baseDate;

    switch(periodType.toUpperCase()) {
      case "WEEKLY" :
        baseDate = today.minusWeeks(1);
        break;
      case "MONTHLY" :
        baseDate = today.minusMonths(1);
        break;
      case "DAILY" :
      default:
        baseDate = lateDates.get(1);
        break;
    }

    List<LocalDate> baseDates = List.of(today,baseDate);

    List<IndexData> dataList = indexQueryRepository.findDataByDatesAndCategory(baseDates, categoryName);
    if (dataList.isEmpty()) {
      return Collections.emptyList();
    }


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

          if (current == null || before == null) return null;

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
        .limit(rankLimit)
        .map(indexPerformanceDto -> {
          return new RankedIndexPerformanceDto(indexPerformanceDto, rankCounter.getAndIncrement());
        })
        .toList();
  }
  @Transactional
  public List<IndexChartDto> getIndexChart(Long indexChartId, String categoryName, String periodType){
    List<LocalDate> lateDates = indexDataRepository.findDistinctByBaseDate(PageRequest.of(0,1));
    if(lateDates == null || lateDates.isEmpty()) {
      throw new NoSuchElementException("데이터가 충분하지 않습니다. 현재 DB 날짜 개수: {}");
    }

    LocalDate endDate = lateDates.get(0);
    LocalDate startDate;

    String type = (periodType != null) ? periodType.toUpperCase() : "MONTHLY";
    switch (type) {
      case "QUARTERLY" :
        startDate = endDate.minusMonths(3);
        break;
      case "YEARLY" :
        startDate = endDate.minusYears(1);
        break;
      case "MONTHLY" :
      default:
        startDate = endDate.minusMonths(1);
        break;
    }

    // 이동평균선(MA20)
    LocalDate fetchStartDate = startDate.minusDays(35);

    List<Long> indexChartIds = new ArrayList<>();
    if(indexChartId != null){
      indexChartIds.add(indexChartId);
    } else if(categoryName != null && !categoryName.isEmpty()) {
      indexChartIds.addAll(indexInfoRepository.findIdsByCategoryName(categoryName));
    }

    if(indexChartIds.isEmpty()) return Collections.emptyList();

    List<IndexData> dataList = indexDataRepository.findChartDataBetweenDates(indexChartIds, fetchStartDate, endDate);

    return dataList.stream()
        .collect(Collectors.groupingBy(data -> data.getIndexInfo().getId()))
        .values().stream()
        .map(indexDataList -> {
          IndexInfo info = indexDataList.get(0).getIndexInfo();

          List<IndexChartDto.DataPoint> basicPoints = new ArrayList<>();
          List<IndexChartDto.DataPoint> ma5Points = new ArrayList<>();
          List<IndexChartDto.DataPoint> ma20Points = new ArrayList<>();

          for (int i= 0; i< indexDataList.size(); i++) {
            IndexData current = indexDataList.get(i);
            LocalDate date = current.getBaseDate();
            BigDecimal price = current.getClosingPrice();

            if(!date.isBefore(startDate)) {
              basicPoints.add(new IndexChartDto.DataPoint(date, price));

              if(i >=4) {
                BigDecimal sum5 = BigDecimal.ZERO;
                for(int j = 0; j < 5; j++){
                  sum5 = sum5.add(indexDataList.get(i - j).getClosingPrice());
                }
                ma5Points.add(new IndexChartDto.DataPoint(date, sum5.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP)));
              }

              if(i >=19) {
                BigDecimal sum20 = BigDecimal.ZERO;
                for(int j = 0; j < 20; j++){
                  sum20 = sum20.add(indexDataList.get(i - j).getClosingPrice());
                }
                ma20Points.add(new IndexChartDto.DataPoint(date, sum20.divide(BigDecimal.valueOf(20), 2, RoundingMode.HALF_UP)));
              }
            }
          }

          return new IndexChartDto(
              info.getId(),
              info.getCategoryName(),
              info.getIndexName(),
              type,
              basicPoints,
              ma5Points,
              ma20Points
          );
        })
        .toList();
  }
}