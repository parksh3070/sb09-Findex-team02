package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.dto.request.IndexInfoUpdateRequest;
import org.example.dto.response.IndexInfoResponseDto;
import org.example.service.IndexInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

    private final IndexInfoService indexInfoService;

    /**
     * 지수 정보 등록
     */
    @PostMapping
    public ResponseEntity<IndexInfoResponseDto> createIndexInfo(
            @RequestBody IndexInfoCreateRequest request
    ) {
        IndexInfoResponseDto response = indexInfoService.createIndexInfo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexInfo(@PathVariable Long id) {
        indexInfoService.deleteIndexInfo(id);
        return ResponseEntity.noContent().build();
    }
}