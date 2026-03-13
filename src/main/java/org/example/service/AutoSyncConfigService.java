package org.example.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.AutoSyncConfigDto;
import org.example.dto.request.AutoSyncConfigSearchRequest;
import org.example.dto.request.AutoSyncConfigUpdateRequest;
import org.example.dto.response.CursorPageResponseAutoSyncConfigDto;
import org.example.entity.IndexInfo;
import org.example.entity.AutoSyncConfig;
import org.example.mapper.AutoSyncConfigMapper;
import org.example.repository.IndexInfoRepository;
import org.example.repository.AutoSyncConfigRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoSyncConfigService {
  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final AutoSyncConfigMapper autoSyncConfigMapper;

  // Index_info 추가 시 생성
  public AutoSyncConfigDto create(Long indexId){

    // index_id 값으로 index_info 객체 조회
    IndexInfo indexInfoProxy = indexInfoRepository.getReferenceById(indexId);
    AutoSyncConfig newAutoSyncConfig = new AutoSyncConfig(indexInfoProxy);

    autoSyncConfigRepository.save(newAutoSyncConfig);

    return autoSyncConfigMapper.toDto(newAutoSyncConfig);
  }

  public AutoSyncConfigDto find(Long id){
    return autoSyncConfigMapper.toDto(autoSyncConfigRepository.findById(id).orElseThrow());
  }

  public CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto> findAll(AutoSyncConfigSearchRequest request){
    Sort.Direction direction = request.sortDirection().equalsIgnoreCase("desc")
        ? Sort.Direction.DESC : Sort.Direction.ASC;

    Sort sort = Sort.by(direction, request.sortField());

    Pageable pageable = PageRequest.of(0, request.size() + 1, sort);

    List<AutoSyncConfig> configs = autoSyncConfigRepository.findAllWithCursor(
        request.indexInfoId(),
        request.enabled(),
        request.idAfter(),
        pageable
    );

    boolean hasNext = configs.size() > request.size();
    List<AutoSyncConfig> content = hasNext
        ? configs.subList(0, request.size())
        : configs;

    List<AutoSyncConfigDto> contentDtoList = content.stream()
        .map(autoSyncConfigMapper::toDto)
        .toList();

    Long nextIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).getId();
    String nextCursor = (nextIdAfter != null) ? String.valueOf(nextIdAfter) : null;

    long totalElements = autoSyncConfigRepository.countByFilters(
        request.indexInfoId(),
        request.enabled()
    );

    return new CursorPageResponseAutoSyncConfigDto<>(
        contentDtoList,
        nextCursor,
        nextIdAfter,
        contentDtoList.size(),
        totalElements,
        hasNext
    );

  }

  public AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request){
    AutoSyncConfig config = autoSyncConfigRepository.findById(id).orElseThrow();
    config.updateEnabled(request.enabled());
    return autoSyncConfigMapper.toDto(config);

  }

  public void delete(Long id){
    autoSyncConfigRepository.deleteById(id);
  }
}
