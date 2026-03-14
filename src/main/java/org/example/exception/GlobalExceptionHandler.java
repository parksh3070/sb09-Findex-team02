package org.example.exception;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class GlobalExceptionHandler {

  // 1. DTO 유효성 검사 실패 400
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    log.warn("Validation Error: {}", errorMessage);

    ErrorResponse response = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        "입력값이 올바르지 않습니다.",
        errorMessage
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 2. 비즈니스 로직 에러 400
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("Business Erro: {}", e.getMessage());

    ErrorResponse response = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        "잘못된 요청입니다.",
        e.getMessage()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 3. 예상치 못한 서버 에러 500
  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
    log.error("Unhandled Exception: ", e);

    ErrorResponse response = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "서버 내부 오류",
        "서버에서 알 수 없는 오류가 발생했습니다."
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
  // 404(데이터가 없을때)
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(NoSuchElementException e) {
    log.warn("Not Found Error: {}", e.getMessage());

    ErrorResponse response = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.NOT_FOUND.value(),
        "리소스를 찾을 수 없습니다.",
        e.getMessage()
    );

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

}
