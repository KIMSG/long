package com.longleg.reward.utils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * CachingRequestBodyFilter
 * HTTP 요청의 본문을 캐싱할 수 있도록 ContentCachingRequestWrapper로 감싸는 필터입니다.
 * 이를 통해 요청 본문을 여러 번 읽을 수 있습니다.
 */
@Component
public class CachingRequestBodyFilter implements Filter {

    /**
     * 필터 실행 메서드
     * @param request  서블릿 요청 객체
     * @param response 서블릿 응답 객체
     * @param chain    필터 체인 객체
     * @throws IOException      I/O 오류 발생 시
     * @throws ServletException 서블릿 관련 오류 발생 시
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // HttpServletRequest인지 확인 후 ContentCachingRequestWrapper로 감싸기
        if (request instanceof HttpServletRequest httpServletRequest) {
            HttpServletRequest wrappedRequest = new ContentCachingRequestWrapper(httpServletRequest);
            chain.doFilter(wrappedRequest, response); // 래핑된 요청 전달
        } else {
            chain.doFilter(request, response); // HttpServletRequest가 아닌 경우 원본 요청 전달
        }
    }
}