package com.ronext.rpdptw.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * [Spring Boot 애플리케이션 기동 및 헬스체크 검증 테스트]
 * 
 * Stage 0 명세 계약(stage-00-cleanup-and-skeleton.md §5.5)에 따라 app 모듈이 정상 기동되는지 확인합니다.
 * 1. 스프링 컨텍스트가 에러 없이 초기화되는지 확인 (contextLoads)
 * 2. 내장 웹 서버의 헬스체크 URL(/actuator/health)이 HTTP 200 OK와 status: "UP"을 반환하는지 확인 (healthEndpointRespondsUp)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class RoNextApplicationTest {

    // 테스트용 HTTP 클라이언트 (랜덤 포트로 뜬 내장 서버와 자동으로 연결됨)
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * [테스트 1] 스프링 컨텍스트 로딩 검증 (T2)
     * - 스프링 설정(application.yml, 빈 등록 등)에 오류가 없는지 실행만으로 검증합니다.
     */
    @Test
    void contextLoads() {
        // Spring context starts (T2)
    }

    /**
     * [테스트 2] Actuator 헬스체크 엔드포인트 응답 검증 (T3)
     * - 내장 웹 서버에 GET /actuator/health 요청을 보내 서버 상태가 정상인지 직접 확인합니다.
     */
    @Test
    void healthEndpointRespondsUp() {
        // 1. /actuator/health 주소로 GET 요청을 보내 응답을 수신 (바디는 Map 객체로 파싱)
        ResponseEntity<Map> response =
                restTemplate.getForEntity("/actuator/health", Map.class);

        // 2. HTTP 상태 코드가 200 OK인지 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3. 응답 바디가 null이 아닌지 검증
        assertThat(response.getBody()).isNotNull();

        // 4. 응답 JSON의 "status" 키 값이 "UP" 인지 검증 ({"status": "UP"})
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }
}


