package com.longleg.reward.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler
 * - 애플리케이션 전역에서 발생하는 예외를 처리합니다.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 500 Internal Server Error 예외 처리
     * @param ex 발생한 예외
     * @param request 요청 정보
     * @return 사용자에게 반환할 오류 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInternalServerError(Exception ex, WebRequest request) {
        // 응답 본문 데이터 구성
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal server error");
        body.put("details", "서버에서 예기치 않은 오류가 발생했습니다. 나중에 다시 시도해 주세요.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    /**
     * 400 Bad Request 예외 처리
     * IllegalArgumentException이 발생했을 때 처리합니다. 요청 데이터가 유효하지 않거나
     * 로직에서 잘못된 값이 전달된 경우 발생합니다.
     *
     * @param ex 발생한 예외
     * @param request 요청 정보
     * @return 사용자에게 반환할 오류 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("details", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * NullPointerException이 발생했을 때 처리합니다. 시스템에서 누락된 데이터에 접근하거나
     * 예상치 못한 Null 값으로 인해 발생하는 문제를 처리합니다.
     *
     * @param ex 발생한 예외
     * @param request 요청 정보
     * @return 사용자에게 반환할 오류 응답
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Null Pointer Exception");
        body.put("details", "필요한 데이터가 누락되었습니다.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * CustomException 처리
     * 커스텀 예외 발생 시 error와 details를 동적으로 반환합니다.
     *
     * @param ex 커스텀 예외
     * @param request 요청 정보
     * @return 사용자에게 반환할 오류 응답
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", ex.getError()); // 커스텀 error 메시지
        body.put("details", ex.getMessage()); // 커스텀 details 메시지

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 400 Bad Request - 유효성 검증 실패 처리
     * @param ex 유효성 검증 예외
     * @param request 요청 정보
     * @return 사용자에게 반환할 오류 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation Failed");
        body.put("details", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 400 Bad Request - JSON 요청 본문 오류 처리
     * @param ex 발생한 예외
     * @param request 요청 정보
     * @return 사용자에게 반환할 오류 응답
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Malformed JSON Request");
        body.put("details", "올바르지 않은 JSON 요청입니다. 확인 후 다시 시도해 주세요.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}