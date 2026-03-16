package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.data.SyncJobDto;
import org.example.service.IntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {
  private final IntegrationService integrationService;

  //지수 정보 연동
  @PostMapping("/index-infos")
  public ResponseEntity<List<SyncJobDto>> syncIndexInfos(HttpServletRequest request) {
    String clientIp = request.getRemoteAddr();

    log.info("지수 정보 동기화 요청 발생 - 접속자 IP: {}", clientIp);
    List<SyncJobDto> results = integrationService.syncIndexInfos(clientIp);
    return ResponseEntity.ok(results);
    }

    //지수 데이터 연동

  }


