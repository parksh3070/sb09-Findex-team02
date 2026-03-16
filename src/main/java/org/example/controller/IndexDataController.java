package org.example.controller;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexDataDto;
import org.example.dto.request.IndexDataCreateRequest;
import org.example.dto.request.IndexDataSearchRequest;
import org.example.dto.request.IndexDataUpdateRequest;
import org.example.service.IndexDataService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/index-data")
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


}