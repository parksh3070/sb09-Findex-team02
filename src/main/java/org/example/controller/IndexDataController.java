package org.example.controller;

import java.time.Instant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexChartDto;
import org.example.dto.data.IndexDataDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataSearchRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.dto.response.FavoritePerformanceResponse;
import org.example.dto.response.RankedIndexPerformanceDto;
import org.example.service.IndexDataService;
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

  @GetMapping
  public List<IndexDataDto> search(IndexDataSearchRequest request) {
    return indexDataService.search(request);
  }

  @PatchMapping("/{indexId}/{baseDate}")
  public Long update(
      @PathVariable Long indexId,
      @PathVariable Instant baseDate,
      @RequestBody IndexDataUpdateRequest request
  ) {
    return indexDataService.update(indexId, baseDate, request);
  }

  @DeleteMapping("/{indexId}/{baseDate}")
  public void delete(
      @PathVariable Long indexId,
      @PathVariable Instant baseDate
  ) {
    indexDataService.delete(indexId, baseDate);
  }


  @Operation(summary = "관심 지수 성과 조회", description = "즐겨찾기한 지수들의 기간별 성과를 요약하여 조회합니다.")
  @GetMapping("/performance/favorite")
  public ResponseEntity<List<FavoritePerformanceResponse>> getFavoritIndexRankings(
      @Schema(allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
      @RequestParam String periodType
  ){
    return ResponseEntity.ok(indexDataService.getFavoritePerformances(periodType));
  }

  @Operation(summary = "지수 성과 랭킹 조회", description = "지수의 성과 분석 랭킹을 조회합니다.")
  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getRankingPerformance(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(required = false) String categoryName,
      @Schema(allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
      @RequestParam(defaultValue = "DAILY") String periodType,
      @RequestParam(defaultValue = "10") Integer rankLimit
  ){
    List<RankedIndexPerformanceDto> result = indexDataService.getPerformanceRanking(indexInfoId,categoryName,periodType,rankLimit);
    return ResponseEntity.ok(result);
  }


  @Operation(summary = "지수 차트 조회", description = "지수 차트 데이터를 조회합니다.")
  @GetMapping("/chart")
  public ResponseEntity<List<IndexChartDto>> getIndexChart(
    @RequestParam(required = false) Long indexId,
    @RequestParam(required = false) String categoryName,
    @Schema(allowableValues = {"MONTHLY", "QUARTERLY", "YEARLY"})
    @RequestParam(defaultValue = "MONTHLY") String periodType
  ){
    List<IndexChartDto> indexCharList = indexDataService.getIndexChart(indexId, categoryName, periodType);
    return ResponseEntity.ok(indexCharList);
  }
}