package org.example.client;

import org.example.dto.response.OpenApiStockResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//외부 데이터 가져오기 위한 인터페이스
@FeignClient(name = "openApiClient", url = "${openapi.base-url}")
public interface IndexApiClient {

  @GetMapping("/getStockMarketIndex")
  OpenApiStockResponseDto getIndexData(
      @RequestParam("serviceKey") String serviceKey,
      @RequestParam("numOfRows") int numOfRows,
      @RequestParam("pageNo") int pageNo,
      @RequestParam("beginBasDt") String beginBasDt,
      @RequestParam("endBasDt") String endBasDt,
      @RequestParam(value = "resultType", defaultValue = "json") String resultType
  );
}
