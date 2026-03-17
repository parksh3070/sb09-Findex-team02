package org.example.api;

import static org.example.api.ApiDocsUtils.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.example.dto.data.SyncJobDto;
import org.example.dto.request.IndexDataSyncRequest;
import org.example.dto.request.SyncJobSearchRequest;
import org.example.dto.response.CursorPageResponseAutoSyncConfigDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "연동 작업 API", description = "연동 작업 관리 API")
public interface SyncJobApi {

  @Operation(summary = "지수 정보 연동", description = "Open API를 통해 지수 정보를 연동합니다.")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = ACCEPTED_202, description = "연동 작업 생성 성공",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = SyncJobDto.class)))
      ),
      @ApiResponse(
          responseCode = BAD_REQUEST_400, description = "잘못된 요청",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 400, \"message\": \"잘못된 요청입니다.\", \"details\": \"부서 코드는 필수입니다.\"}"))
      ),
      @ApiResponse(
          responseCode = INTERNAL_SERVER_ERROR_500, description = "서버 오류",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 500, \"message\": \"서버 내부 오류입니다.\", \"details\": \"서버에서 알 수 없는 오류가 발생했습니다.\"}"))
      )
  })
  ResponseEntity<List<SyncJobDto>> syncIndexInfos(HttpServletRequest request);

  @Operation(summary = "지수 데이터 연동", description = "Open API를 통해 지수 데이터를 연동합니다.")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = ACCEPTED_202, description = "연동 작업 생성 성공",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = SyncJobDto.class)))
      ),
      @ApiResponse(
          responseCode = BAD_REQUEST_400, description = "잘못된 요청(유효하지 않은 날짜 범위 등)",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 400, \"message\": \"잘못된 요청입니다.\", \"details\": \"부서 코드는 필수입니다.\"}"))
      ),
      @ApiResponse(
          responseCode = NOT_FOUND_404, description = "지수 정보를 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 404, \"message\": \"조회 결과가 없습니다.\", \"details\": \"해당 ID의 지수 정보가 존재하지 않습니다.\"}"))
      ),
      @ApiResponse(
          responseCode = INTERNAL_SERVER_ERROR_500, description = "서버 오류",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 500, \"message\": \"서버 내부 오류입니다.\", \"details\": \"서버에서 알 수 없는 오류가 발생했습니다.\"}"))
      )
  })
  ResponseEntity<List<SyncJobDto>> syncIndexData(
      @Parameter(description = "지수 데이터 연동 요청 정보") IndexDataSyncRequest syncRequest,
      HttpServletRequest request
  );

  @Operation(summary = "연동 작업 목록 조회", description = "연동 작업 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ResponseStatus(HttpStatus.OK)
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = SUCCESS_200, description = "연동 작업 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = CursorPageResponseAutoSyncConfigDto.class))
      ),
      @ApiResponse(
          responseCode = BAD_REQUEST_400, description = "잘못된 요청(유효하지 않은 날짜 범위 등)",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 400, \"message\": \"잘못된 요청입니다.\", \"details\": \"부서 코드는 필수입니다.\"}"))
      ),
      @ApiResponse(
          responseCode = INTERNAL_SERVER_ERROR_500, description = "서버 오류",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 500, \"message\": \"서버 내부 오류입니다.\", \"details\": \"서버에서 알 수 없는 오류가 발생했습니다.\"}"))
      )
  })
  ResponseEntity<CursorPageResponseAutoSyncConfigDto<SyncJobDto>> getSyncJobs(
      @Parameter(description = "조회 필터 및 페이지네이션 파라미터") SyncJobSearchRequest request
  );
}