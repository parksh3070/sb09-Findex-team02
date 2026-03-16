package org.example.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexInfoDto;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.entity.AutoSyncConfig;
import org.example.entity.IndexInfo;
import org.example.entity.type.SourceType;
import org.example.repository.AutoSyncConfigRepository;
import org.example.repository.IndexInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional

//지수정보 관련 로직 클래스 작성
public class IndexInfoService {


    private final IndexInfoRepository indexInfoRepository;
    private final AutoSyncConfigRepository autoSyncConfigRepository;

    public IndexInfoDto createIndexInfo(IndexInfoCreateRequest request) {

        // 1. 중복 검사 > 지수분류명 + 지수명 조합은 중복불가해야함
        //변수에 담아서 확인해야함
        boolean exists = indexInfoRepository.existsByCategoryNameAndIndexName(
                // 요청DTO에 지수분류명 꺼냄
                request.indexClassification(),
                //요청DTO에 지수명 꺼냄
                request.indexName()
        );
        // 같은조합있으면 예외로 던지기
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 지수 정보입니다.");
        }

        // 2. 지수 정보 생성

        IndexInfo indexInfo = new IndexInfo(
                request.indexClassification(),
                request.indexName(),
                SourceType.USER
        );
        //기준 시점, 기준 지수, 채용 종목 수를 채워넣기
        indexInfo.setIndexDetails(
                request.basePointInTime().atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
                BigDecimal.valueOf(request.baseIndex()),
                request.employedItemsCount()
        );

        indexInfo.updateFavorite(request.favorite());

        // 3. 저장
        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

        // 4. 자동 연동 설정 초기화
        AutoSyncConfig autoSyncConfig = new AutoSyncConfig(savedIndexInfo);
        autoSyncConfigRepository.save(autoSyncConfig);

        // 5. DTO 반환
        return new IndexInfoDto(
                savedIndexInfo.getId(),
                savedIndexInfo.getCategoryName(),
                savedIndexInfo.getIndexName(),
                savedIndexInfo.getComponent(),
                savedIndexInfo.getBaseDate().atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                savedIndexInfo.getBaseIndex().doubleValue(),
                savedIndexInfo.getSourceType(),
                savedIndexInfo.getFavorite()
        );
    }
}