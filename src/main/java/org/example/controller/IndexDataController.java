package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.IndexDataCreateRequest;
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
}