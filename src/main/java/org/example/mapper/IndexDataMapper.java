package org.example.mapper;

import org.example.dto.data.IndexDataDto;
import org.example.entity.IndexData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IndexDataMapper {

  @Mapping(source = "indexInfo.id", target = "indexInfoId")
  IndexDataDto toDto(IndexData entity);
}