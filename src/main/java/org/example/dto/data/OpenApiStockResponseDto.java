package org.example.dto.data;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenApiStockResponseDto(Response response) {
  public record Response(Header header){
    public record Header(String resultCode, String resultMsg){
      public record Body(
          int numOfRows,
          int pageNo,
          int totalCount,
          Items items
      ){}

      public record Items(List<Item> item){}

      public record Item(
          @JsonProperty("idxCsf") String CategoryName,            // 지수분류명
          @JsonProperty("idxNm") String indexName,                // 지수명
          @JsonProperty("epyItmsCnt") Integer componentCount,     // 채용종목 수
          @JsonProperty("basPntm") String infoBaseDate,           // 기준 시점
          @JsonProperty("basIdx") Double baseIndex,               // 기준 지수

          @JsonProperty("basDt") String dataBaseDate,             // 기준 일자
          @JsonProperty("mkp") Double openPrice,                  // 시가
          @JsonProperty("clpr") Double closePrice,                // 종가
          @JsonProperty("hipr") Double highPrice,                 // 고가
          @JsonProperty("lopr") Double lowPrice,                  // 저가
          @JsonProperty("vs") Double priceDiff,                   // 대비
          @JsonProperty("fltRt") Double fluctuationRate,          // 등락률
          @JsonProperty("trqu") Long tradeVolume,                 // 거래량
          @JsonProperty("trPrc") Long tradeAmount,                // 거래 대금
          @JsonProperty("lstgMrktToAmt") Long marketCap           // 상장 시가 총액
      ){}
    }
  }
}
