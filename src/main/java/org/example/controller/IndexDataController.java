package org.example.controller;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexDataDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataSearchRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.dto.response.CursorPageResponseIndexDataDto;
import org.example.dto.response.FavoritePerformanceResponse;
import org.example.service.IndexDataService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "지수 데이터")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  @PostMapping
  public Long create(@RequestBody IndexDataCreateRequest request) {
    return indexDataService.create(request);
  }


  @GetMapping("/index-data")
  public CursorPageResponseIndexDataDto<IndexDataDto> search(
      @RequestBody IndexDataSearchRequest request
  ) {
    return indexDataService.search(request);
  }

  @PatchMapping("/{indexId}/{baseDate}")
  public Long update(
      @PathVariable Long indexId,
      @PathVariable LocalDate baseDate,
      @RequestBody IndexDataUpdateRequest request
  ) {
    return indexDataService.update(indexId, baseDate, request);
  }

  @DeleteMapping("/{indexId}/{baseDate}")
  public void delete(
      @PathVariable Long indexId,
      @PathVariable LocalDate baseDate
  ) {
    indexDataService.delete(indexId, baseDate);
  }



  @Operation(summary = "관심 지수 성과 조회", description = "즐겨찾기한 지수들의 기간별 성과를 요약하여 조회합니다.")
  @GetMapping("/performance/favorite")
  public ResponseEntity<List<FavoritePerformanceResponse>> getFavoriteIndexRankings(
      @Schema(allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
      @RequestParam String periodType
  ){
    return ResponseEntity.ok(indexDataService.getFavoritePerformances(periodType));
  }

  @PostMapping("/index-data/export")
  public ResponseEntity<InputStreamResource> export(
      @RequestBody IndexDataSearchRequest request
  ) {

    ByteArrayInputStream csv = indexDataService.export(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=index-data.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(new InputStreamResource(csv));
  }


}