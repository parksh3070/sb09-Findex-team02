package org.example.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IndexInfoUpdateRequest {

    // 채용 종목 수
    private Integer component;

    // 기준 시점
    private LocalDate baseDate;

    // 기준 지수
    private BigDecimal baseIndex;

    // 즐겨찾기
    private Boolean favorite;
}