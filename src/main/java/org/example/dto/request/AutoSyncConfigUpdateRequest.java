package org.example.dto.request;

import jakarta.validation.constraints.NotNull;

public record AutoSyncConfigUpdateRequest(
    @NotNull(message = "활성화 주기는 필수 입니다.")
    Boolean enabled
) {

}
