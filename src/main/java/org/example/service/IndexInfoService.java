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

    @Transactional
    public IndexInfoResponseDto createIndexInfo(IndexInfoCreateRequest request) {

        boolean exists = indexInfoRepository.existsByCategoryNameAndIndexName(
                request.indexClassification(),
                request.indexName()
        );

        if (exists) {
            throw new IllegalArgumentException("이미 같은 지수 분류명과 지수명을 가진 지수 정보가 존재합니다.");
        }

        IndexInfo indexInfo = new IndexInfo(
                request.indexClassification(),
                request.indexName(),
                request.sourceType()
        );

        indexInfo.setIndexDetails(
                request.basePointInTime(),
                request.baseIndex(),
                request.employedItemsCount()
        );

        if (request.favorite() != null) {
            indexInfo.updateFavorite(request.favorite());
        }

        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

        autoSyncConfigService.create(savedIndexInfo.getId());

        return IndexInfoResponseDto.from(savedIndexInfo);
    }

    @Transactional
    public IndexInfoResponseDto updateIndexInfo(Long id, IndexInfoUpdateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        Integer component = request.employedItemsCount() != null
                ? request.employedItemsCount()
                : indexInfo.getComponent();

        LocalDate baseDate = request.basePointInTime() != null
                ? request.basePointInTime()
                : indexInfo.getBaseDate();

        BigDecimal baseIndex = request.baseIndex() != null
                ? request.baseIndex()
                : indexInfo.getBaseIndex();

        indexInfo.setIndexDetails(baseDate, baseIndex, component);

        if (request.favorite() != null) {
            indexInfo.updateFavorite(request.favorite());
        }

        return IndexInfoResponseDto.from(indexInfo);
    }

    @Transactional
    public void deleteIndexInfo(Long id) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보를 찾을 수 없습니다. id=" + id));

        indexInfoRepository.delete(indexInfo);
    }
}