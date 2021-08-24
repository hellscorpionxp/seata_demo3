package com.tony.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.seata.spring.annotation.GlobalTransactional;

@RestController
public class BusinessController {

  private static final String SUCCESS = "SUCCESS";
  private static final String FAIL = "FAIL";
  @Autowired
  private RestTemplate restTemplate;

  @GlobalTransactional(timeoutMills = 300000, name = "seata_demo")
  @PostMapping(value = "/business", produces = "application/json")
  public String account(String commodityCode, int count, String userId) {
    String result = restTemplate.postForObject(
        String.format("http://127.0.0.1:18083/stock?commodityCode=%s&count=%d", commodityCode, count),
        new LinkedMultiValueMap<>(), String.class);
    if (!SUCCESS.equals(result)) {
      throw new RuntimeException();
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("userId", userId);
    map.add("commodityCode", commodityCode);
    map.add("orderCount", count + "");
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
    ResponseEntity<String> response;
    try {
      response = restTemplate.postForEntity("http://127.0.0.1:18082/order", request, String.class);
    } catch (Exception e) {
      throw new RuntimeException("mock error");
    }
    result = response.getBody();
    if (!SUCCESS.equals(result)) {
      throw new RuntimeException();
    }
    return SUCCESS;
  }

}
