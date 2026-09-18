package com.clearing.netting.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClearingCalendarIntegrationTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private TestRestTemplate rest;

    @Test
    void holidayBlocksNettingAndViewerIsReadOnly() throws Exception {
        String operator = login("operator", "op123456");
        String viewer = login("viewer", "view123456");
        LocalDate target = LocalDate.of(2026, 9, 18);

        // 只读账户不能添加假日
        ResponseEntity<String> denied = postJson(
                "/api/clearing-calendar/holidays",
                viewer,
                Map.of("date", target.toString(), "name", "测试假日"));
        assertEquals(HttpStatus.FORBIDDEN, denied.getStatusCode());

        // 操作员添加假日
        ResponseEntity<String> added = postJson(
                "/api/clearing-calendar/holidays",
                operator,
                Map.of("date", target.toString(), "name", "测试假日"));
        assertEquals(HttpStatus.OK, added.getStatusCode());
        assertTrue(added.getBody().contains(target.toString()));

        // 只读账户能看到假日
        ResponseEntity<String> list = getJson("/api/clearing-calendar/holidays", viewer);
        assertEquals(HttpStatus.OK, list.getStatusCode());
        assertTrue(list.getBody().contains(target.toString()));

        // 目标日是假日 -> 轧差必须明确报错
        ResponseEntity<String> blocked = postJson(
                "/api/netting-runs",
                operator,
                Map.of("settleDate", target.toString(), "currency", "USD"));
        assertEquals(HttpStatus.BAD_REQUEST, blocked.getStatusCode());
        JsonNode errBody = JSON.readTree(blocked.getBody());
        assertEquals("SETTLE_DATE_HOLIDAY", errBody.get("code").asText());
        assertTrue(errBody.get("message").asText().contains("holiday"));

        // 删除假日后校验放行（无义务时会得到业务错误，但不应再是假日错误，且不应产生 FAILED 批次）
        ResponseEntity<String> removed = exchange(
                "/api/clearing-calendar/holidays/" + target,
                HttpMethod.DELETE,
                operator,
                null);
        assertEquals(HttpStatus.OK, removed.getStatusCode());

        ResponseEntity<String> afterRemove = postJson(
                "/api/netting-runs",
                operator,
                Map.of("settleDate", target.toString(), "currency", "USD"));
        JsonNode afterBody = JSON.readTree(afterRemove.getBody());
        assertTrue(afterBody.has("code"));
        assertTrue(!"SETTLE_DATE_HOLIDAY".equals(afterBody.get("code").asText()),
                "holiday removed, must not be blocked by calendar");
    }

    private String login(String username, String password) throws Exception {
        ResponseEntity<String> resp = exchangeRaw(
                "/api/auth/login",
                HttpMethod.POST,
                JSON.writeValueAsString(Map.of("username", username, "password", password)));
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return JSON.readTree(resp.getBody()).get("token").asText();
    }

    private ResponseEntity<String> getJson(String path, String token) {
        return exchange(path, HttpMethod.GET, token, null);
    }

    private ResponseEntity<String> postJson(String path, String token, Object body) throws Exception {
        return exchange(path, HttpMethod.POST, token, JSON.writeValueAsString(body));
    }

    private ResponseEntity<String> exchangeRaw(String path, HttpMethod method, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(path, method, new HttpEntity<>(jsonBody, headers), String.class);
    }

    private ResponseEntity<String> exchange(String path, HttpMethod method, String token, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        if (jsonBody != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return rest.exchange(path, method, new HttpEntity<>(jsonBody, headers), String.class);
    }
}
