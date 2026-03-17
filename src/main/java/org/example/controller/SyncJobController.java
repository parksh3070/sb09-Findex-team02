package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.api.SyncJobApi;
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

@Slf4j
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobApi {
  private final IntegrationService integrationService;

  //지수 정보 연동
  @PostMapping("/index-infos")
  public ResponseEntity<List<SyncJobDto>> syncIndexInfos(HttpServletRequest request) {
    String worker = request.getRemoteAddr();
    List<SyncJobDto> results = integrationService.syncIndexInfos(worker);
    return ResponseEntity.ok(results);
    }

    //지수 데이터 연동
    @PostMapping("/index-data")
    public ResponseEntity<List<SyncJobDto>> syncIndexData(
        @RequestBody IndexDataSyncRequest syncRequest,
        HttpServletRequest request) {

      String worker = request.getRemoteAddr();
      List<SyncJobDto> totalResults = new ArrayList<>();
      List<Long> targetIdList = syncRequest.indexInfoIds();

      if (syncRequest.indexInfoIds() == null || syncRequest.indexInfoIds().isEmpty()) {
        totalResults.addAll(integrationService.syncIndexData(
            worker, syncRequest.baseDateFrom(), syncRequest.baseDateTo(), null));
      } else {
        totalResults.addAll(integrationService.syncIndexData(
            worker, syncRequest.baseDateFrom(), syncRequest.baseDateTo(), targetIdList));
      }
      return ResponseEntity.accepted().body(totalResults);
    }

    //연동 작업 목록 조회
    @GetMapping
    public ResponseEntity<CursorPageResponseAutoSyncConfigDto<SyncJobDto>> getSyncJobs(
        @ModelAttribute SyncJobSearchRequest request
    ) {
      log.info("[연동 작업 목록 조회] 파라미터: {}", request);

      CursorPageResponseAutoSyncConfigDto<SyncJobDto> response = integrationService.getSyncJobs(request);
      return ResponseEntity.ok(response);
    }
  }


