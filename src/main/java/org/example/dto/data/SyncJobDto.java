package org.example.dto.data;


import java.time.LocalDateTime;

public record SyncJobDto(
    Long id,
    String jobType,
    Long indexInfoId,
    String targetDate,
    String worker,
    LocalDateTime jobTime,
    String result
) {

}
