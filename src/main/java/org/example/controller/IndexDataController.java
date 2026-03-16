package org.example.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.response.FavoritePerformanceResponse;
import org.example.service.IndexDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

  @Operation(summary = "관심 지수 성과 조회", description = "즐겨찾기한 지수들의 기간별 성과를 요약하여 조회합니다.")
  @GetMapping("/performance/favorite")
  public ResponseEntity<List<FavoritePerformanceResponse>> getFavoritIndexRankings(
      @Schema(allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
      @RequestParam String periodType
  ){
    return ResponseEntity.ok(indexDataService.getFavoritePerformances(periodType));
  }
}