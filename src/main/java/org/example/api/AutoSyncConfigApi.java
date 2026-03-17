package org.example.api;
import static org.example.api.ApiDocsUtils.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.data.AutoSyncConfigDto;
import org.example.dto.request.AutoSyncConfigSearchRequest;
import org.example.dto.request.AutoSyncConfigUpdateRequest;
import org.example.dto.response.CursorPageResponseAutoSyncConfigDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "자동 연동 설정 API", description = "자동 연동 설정 관리 API")
public interface AutoSyncConfigApi {
  @Operation(summary = "자동 연동 설정 단건 조회", description = "ID를 통해 특정 자동 연동 설정을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = SUCCESS_200, description = "조회 성공",
          content = @Content(schema = @Schema(implementation = AutoSyncConfigDto.class))
      ),
      @ApiResponse(
          responseCode = NOT_FOUND_404, description = "설정을 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 404, \"message\": \"조회 결과가 없습니다.\", \"details\": \"해당 ID의 지수 정보가 존재하지 않습니다.\"}"))
      )
  })
  ResponseEntity<AutoSyncConfigDto> findById(
      @Parameter(description = "자동 연동 설정 ID") @PathVariable Long configId
  );

  @Operation(summary = "자동 연동 설정 수정", description = "기존 자동 연결 설정의 활성화 여부를 수정합니다.")
  @ResponseStatus(HttpStatus.OK)
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = SUCCESS_200, description = "자동 연동 설정 수정 성공",
          content = @Content(schema = @Schema(implementation = AutoSyncConfigDto.class))
      ),
      @ApiResponse(
          responseCode = BAD_REQUEST_400, description = "잘못된 요청 (유효하지 않은 설정 값 등)",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 400, \"message\": \"잘못된 요청입니다.\", \"details\": \"부서 코드는 필수입니다.\"}"))
      ),
      @ApiResponse(
          responseCode = NOT_FOUND_404, description = "수정할 자동 연동 설정을 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 404, \"message\": \"조회 결과가 없습니다.\", \"details\": \"해당 ID의 지수 정보가 존재하지 않습니다.\"}"))
      ),
      @ApiResponse(
          responseCode = INTERNAL_SERVER_ERROR_500, description = "서버 오류",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 500, \"message\": \"서버 내부 오류입니다.\", \"details\": \"서버에서 알 수 없는 오류가 발생했습니다.\"}"))
      )
  })
  ResponseEntity<AutoSyncConfigDto> updateEnabled(
      @Parameter(description = "자동 연동 설정 ID") Long id,
      @Parameter(description = "수정할 자동 연동 설정 정보") AutoSyncConfigUpdateRequest request
  );

  @Operation(summary = "자동 연동 설정 목록 조회", description = "자동 연동 설정 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = SUCCESS_200, description = "자동 연동 설정 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = CursorPageResponseAutoSyncConfigDto.class))
      ),
      @ApiResponse(
          responseCode = BAD_REQUEST_400, description = "잘못된 요청 (유효하지 않은 필터 값 등)",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 400, \"message\": \"잘못된 요청입니다.\", \"details\": \"부서 코드는 필수입니다.\"}"))
      ),
      @ApiResponse(
          responseCode = INTERNAL_SERVER_ERROR_500, description = "서버 오류",
          content = @Content(examples = @ExampleObject(value = "{\"timestamp\": \"2025-03-06T05:39:06.152068Z\", \"status\": 500, \"message\": \"서버 내부 오류입니다.\", \"details\": \"서버에서 알 수 없는 오류가 발생했습니다.\"}"))
      )
  })
  ResponseEntity<CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto>> findConfigsByCursor(
      @Parameter(description = "페이지네이션 및 필터 파라미터") AutoSyncConfigSearchRequest request
  );
}