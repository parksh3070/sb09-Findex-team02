package org.example.mapper;

import org.example.dto.data.AutoSyncConfigDto;
import org.example.entity.AutoSyncConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AutoSyncConfigMapper {
  AutoSyncConfigDto toDto(AutoSyncConfig autoSyncConfig);
}
