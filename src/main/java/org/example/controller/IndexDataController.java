package org.example.controller;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexChartDto;
import org.example.dto.data.IndexDataDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataSearchRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.dto.response.CursorPageResponseIndexDataDto;
import org.example.dto.response.FavoritePerformanceResponse;
import org.example.dto.response.RankedIndexPerformanceDto;
import org.example.service.IndexDataService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  // 생성 (POST)
  @Operation(summary = "지수 데이터 등록", description = "새로운 지수 데이터를 등록합니다.")
  @PostMapping
  public Long create(@RequestBody IndexDataCreateRequest request) {
    return indexDataService.create(request);
  }

  // 목록 조회 (GET)
  @Operation(summary = "지수 데이터 목록 조회", description = "지수 데이터 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @GetMapping
  public CursorPageResponseIndexDataDto<IndexDataDto> search(
      @ModelAttribute IndexDataSearchRequest request
  ) {
    return indexDataService.search(request);
  }

  // 수정 (PATCH)
  @Operation(summary = "지수 데이터 수정", description = "기존 지수 데이터를 수정합니다.")
  @PatchMapping("/{id}")
  public Long update(
      @PathVariable Long id,
      @PathVariable LocalDate baseDate,
      @RequestBody IndexDataUpdateRequest request
  ) {
    return indexDataService.update(id, baseDate, request);
  }

  // 삭제 (DELETE)
  @Operation(summary = "지수 데이터 삭제", description = "지수 데이터를 삭제합니다.")
  @DeleteMapping("/{id}")
  public void delete(
      @PathVariable Long id,
      @PathVariable LocalDate baseDate
  ) {
    indexDataService.delete(id, baseDate);
  }


  @Operation(summary = "관심 지수 성과 조회", description = "즐겨찾기한 지수들의 기간별 성과를 요약하여 조회합니다.")
  @GetMapping("/performance/favorite")
  public ResponseEntity<List<FavoritePerformanceResponse>> getFavoriteIndexRankings(
      @Schema(allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
      @RequestParam String periodType
  ){
    return ResponseEntity.ok(indexDataService.getFavoritePerformances(periodType));
  }

  @Operation(summary = "지수 데이터를 CSV export", description = "지수 데이터를 CSV 파일로 export합니다.")
  @GetMapping("/export/csv")
  public ResponseEntity<InputStreamResource> export(
      @ModelAttribute IndexDataSearchRequest request
  ) {

    ByteArrayInputStream csv = indexDataService.export(request);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=index-data.csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(new InputStreamResource(csv));
  }


  @Operation(summary = "지수 성과 랭킹 조회", description = "지수의 성과 분석 랭킹을 조회합니다.")
  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getRankingPerformance(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(required = false) String indexName,
      @Schema(allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
      @RequestParam(defaultValue = "DAILY") String periodType,
      @RequestParam(defaultValue = "10") Integer rankLimit
  ){
    List<RankedIndexPerformanceDto> result = indexDataService.getPerformanceRanking(indexInfoId, indexName, periodType, rankLimit);
    return ResponseEntity.ok(result);
  }


  @Operation(summary = "지수 차트 조회", description = "지수 차트 데이터를 조회합니다.")
  @GetMapping("/chart")
  public ResponseEntity<List<IndexChartDto>> getIndexChart(
    @RequestParam(required = false) Long indexChartId,
    @RequestParam(required = false) String indexName,
    @Schema(allowableValues = {"MONTHLY", "QUARTERLY", "YEARLY"})
    @RequestParam(defaultValue = "MONTHLY") String periodType
  ){
    List<IndexChartDto> indexCharList = indexDataService.getIndexChart(indexChartId, indexName, periodType);
    return ResponseEntity.ok(indexCharList);
  }
}