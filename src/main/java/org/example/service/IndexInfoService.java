package org.example.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.dto.request.IndexInfoUpdateRequest;
import org.example.dto.response.IndexInfoDto;
import org.example.entity.IndexInfo;
import org.example.repository.IndexInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

    private final IndexInfoRepository indexInfoRepository;
    private final AutoSyncConfigService autoSyncConfigService;

    @Transactional
    public IndexInfoDto createIndexInfo(IndexInfoCreateRequest request) {

        boolean exists = indexInfoRepository.existsByCategoryNameAndIndexName(
                request.getCategoryName(),
                request.getIndexName()
        );

        if (exists) {
            throw new IllegalArgumentException("이미 같은 지수 분류명과 지수명을 가진 지수 정보가 존재합니다.");
        }

        IndexInfo indexInfo = new IndexInfo(
                request.getCategoryName(),
                request.getIndexName(),
                request.getSourceType()
        );

        indexInfo.setIndexDetails(
                request.getBaseDate(),
                request.getBaseIndex(),
                request.getComponent()
        );

        if (request.getFavorite() != null) {
            indexInfo.updateFavorite(request.getFavorite());
        }

        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

        // 자동 연동 설정 초기화
        autoSyncConfigService.create(savedIndexInfo.getId());

        return IndexInfoDto.from(savedIndexInfo);
    }

    @Transactional
    public IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        Integer component = request.getComponent() != null
                ? request.getComponent()
                : indexInfo.getComponent();

        LocalDate baseDate = request.getBaseDate() != null
                ? request.getBaseDate()
                : indexInfo.getBaseDate();

        BigDecimal baseIndex = request.getBaseIndex() != null
                ? request.getBaseIndex()
                : indexInfo.getBaseIndex();

        indexInfo.setIndexDetails(baseDate, baseIndex, component);

        if (request.getFavorite() != null) {
            indexInfo.updateFavorite(request.getFavorite());
        }

        return IndexInfoDto.from(indexInfo);
    }

    @Transactional
    public void deleteIndexInfo(Long id) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        indexInfoRepository.delete(indexInfo);
    }
}