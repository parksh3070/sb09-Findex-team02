package org.example.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.example.dto.data.SyncJobDto;
import org.example.entity.IntegrationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyncJobMapper {
  @Mapping(source = "indexInfo.id", target = "indexInfoId")
  SyncJobDto toDto(IntegrationLog integrationLog);
  default LocalDate map(Instant instant) {
    if (instant == null) {
      return null;
    }
    return instant.atZone(ZoneId.of("Asia/Seoul")).toLocalDate();
  }
}
