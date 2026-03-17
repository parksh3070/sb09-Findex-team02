package org.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.dto.request.IndexInfoUpdateRequest;
import org.example.dto.response.IndexInfoResponseDto;
import org.example.entity.IndexInfo;
import org.example.repository.IndexInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

    private final IndexInfoRepository indexInfoRepository;
    private final AutoSyncConfigService autoSyncConfigService;

    /**
     * 지수 정보 등록
     */
    @Transactional
    public IndexInfoResponseDto createIndexInfo(IndexInfoCreateRequest request) {

        // 같은 지수 분류명 + 지수명 조합이 이미 존재하는지 확인
        boolean exists = indexInfoRepository.existsByCategoryNameAndIndexName(
                request.categoryName(),
                request.indexName()
        );

        if (exists) {
            throw new IllegalArgumentException("이미 같은 지수 분류명과 지수명을 가진 지수 정보가 존재합니다.");
        }

        // 엔티티 생성
        IndexInfo indexInfo = new IndexInfo(
                request.categoryName(),
                request.indexName(),
                request.sourceType()
        );

        // 상세 정보 세팅
        indexInfo.setIndexDetails(
                request.baseDate(),
                request.baseIndex(),
                request.component()
        );

        // favorite 값이 null이 아니면 요청값으로 반영
        if (request.favorite() != null) {
            indexInfo.updateFavorite(request.favorite());
        }

        // 저장
        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

        // 자동 연동 설정 초기화
        autoSyncConfigService.create(savedIndexInfo.getId());

        // 응답 DTO 반환
        return IndexInfoResponseDto.from(savedIndexInfo);
    }

    /**
     * 지수 정보 수정
     * null 값은 기존 값을 유지하는 Patch 방식
     */
    @Transactional
    public IndexInfoResponseDto updateIndexInfo(Long id, IndexInfoUpdateRequest request) {

        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        // null이면 기존 값 유지
        Integer component = request.component() != null
                ? request.component()
                : indexInfo.getComponent();

        LocalDate baseDate = request.baseDate() != null
                ? request.baseDate()
                : indexInfo.getBaseDate();

        BigDecimal baseIndex = request.baseIndex() != null
                ? request.baseIndex()
                : indexInfo.getBaseIndex();

        // 수정 반영
        indexInfo.setIndexDetails(baseDate, baseIndex, component);

        if (request.favorite() != null) {
            indexInfo.updateFavorite(request.favorite());
        }

        return IndexInfoResponseDto.from(indexInfo);
    }

    /**
     * 지수 정보 삭제
     * 연관된 IndexData는 cascade 설정으로 함께 삭제
     */
    @Transactional
    public void deleteIndexInfo(Long id) {

        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        indexInfoRepository.delete(indexInfo);
    }
}