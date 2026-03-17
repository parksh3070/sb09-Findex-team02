package org.example.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoUpdateRequest(
        Integer component,
        LocalDate baseDate,
        BigDecimal baseIndex,
        Boolean favorite
) {
}