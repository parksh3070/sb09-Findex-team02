package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.data.IndexInfoDto;
import org.example.dto.request.IndexInfoCreateRequest;
import org.example.service.IndexInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

    private final IndexInfoService indexInfoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    //메서드 요청받는부분
    public IndexInfoDto createIndexInfo(@RequestBody IndexInfoCreateRequest request) {
        return indexInfoService.createIndexInfo(request);
    }
}