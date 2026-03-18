package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.dto.request.IndexInfoSearchRequest;
import org.example.dto.request.IndexInfoUpdateRequest;
import org.example.dto.response.CursorPageResponseIndexInfoDto;
import org.example.dto.response.IndexInfoResponseDto;
import org.example.service.IndexInfoService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지수 정보")
@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

    private final IndexInfoService indexInfoService;

    /**
     * 지수 정보 등록
     */
    @Operation(summary = "지수 정보 등록")
    @PostMapping
    public ResponseEntity<IndexInfoResponseDto> createIndexInfo(
            @RequestBody IndexInfoCreateRequest request
    ) {
        IndexInfoResponseDto response = indexInfoService.createIndexInfo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 지수 정보 목록 조회
     */
    @Operation(
            summary = "지수 정보 목록 조회",
            description = "지수 분류명, 지수명, 즐겨찾기 조건으로 지수 정보 목록을 조회합니다. "
                    + "지수 분류명과 지수명은 부분 일치 검색을 지원하며, "
                    + "정렬 및 cursor 기반 페이지네이션을 지원합니다."
    )
    @GetMapping
    public ResponseEntity<CursorPageResponseIndexInfoDto<IndexInfoResponseDto>> findIndexInfosByCursor(
            @ParameterObject IndexInfoSearchRequest request
    ) {
        return ResponseEntity.ok(indexInfoService.findIndexInfosByCursor(request));
    }

    /**
     * 지수 정보 수정
     */
    @Operation(summary = "지수 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<IndexInfoResponseDto> updateIndexInfo(
            @PathVariable Long id,
            @RequestBody IndexInfoUpdateRequest request
    ) {
        IndexInfoResponseDto response = indexInfoService.updateIndexInfo(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 지수 정보 삭제
     * IndexInfo 삭제 시 연관된 IndexData도 함께 삭제됨
     */
    @Operation(summary = "지수 정보 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexInfo(@PathVariable Long id) {
        indexInfoService.deleteIndexInfo(id);
        return ResponseEntity.noContent().build();
    }
}