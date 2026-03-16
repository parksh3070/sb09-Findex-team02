package org.example.service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexDataDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataSearchRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.entity.IndexData;
import org.example.entity.IndexInfo;
import org.example.mapper.IndexDataMapper;
import org.example.repository.IndexDataRepository;
import org.example.repository.IndexInfoRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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

    Instant baseDate = request.baseDate();

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
      IndexInfo indexInfo, Instant baseDate) {
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



}