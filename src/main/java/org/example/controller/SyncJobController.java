package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.data.SyncJobDto;
import org.example.dto.request.IndexDataSyncRequest;
import org.example.dto.request.SyncJobSearchRequest;
import org.example.dto.response.CursorPageResponseAutoSyncConfigDto;
import org.example.service.IntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "연동 API", description = "Open API 기반 지수 정보 및 지수 데이터 연동")
@Slf4j
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {
  private final IntegrationService integrationService;

  //지수 정보 연동
  @PostMapping("/index-infos")
  @Operation(summary = "지수 정보 연동", description = "Open API를 통해 지수 정보를 연동합니다.")
  public ResponseEntity<List<SyncJobDto>> syncIndexInfos(HttpServletRequest request) {
    String worker = request.getRemoteAddr();
    List<SyncJobDto> results = integrationService.syncIndexInfos(worker);
    return ResponseEntity.ok(results);
    }

    //지수 데이터 연동
    @PostMapping("/index-data")
    @Operation(summary = "지수 데이터 연동", description = "Open API를 통해 지수 데이터를 연동합니다.")
    public ResponseEntity<List<SyncJobDto>> syncIndexData(
        @RequestBody IndexDataSyncRequest syncRequest,
        HttpServletRequest request) {

      String worker = request.getRemoteAddr();
      List<SyncJobDto> totalResults = new ArrayList<>();

      if (syncRequest.indexInfoIds() == null || syncRequest.indexInfoIds().isEmpty()) {
        totalResults.addAll(integrationService.syncIndexData(
            worker, syncRequest.baseDateFrom(), syncRequest.baseDateTo(), null));
      } else {
        for (Long id : syncRequest.indexInfoIds()) {
          totalResults.addAll(integrationService.syncIndexData(
              worker, syncRequest.baseDateFrom(), syncRequest.baseDateTo(), id));
        }
      }
      return ResponseEntity.accepted().body(totalResults);
    }

    //연동 작업 목록 조회
    @Operation(summary = "연동 작업 목록 조회", description = "필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
    @GetMapping
    public ResponseEntity<CursorPageResponseAutoSyncConfigDto<SyncJobDto>> getSyncJobs(
        @ModelAttribute SyncJobSearchRequest request
    ) {
      log.info("[연동 작업 목록 조회] 파라미터: {}", request);

      CursorPageResponseAutoSyncConfigDto<SyncJobDto> response = integrationService.getSyncJobs(request);
      return ResponseEntity.ok(response);
    }
  }


