package org.example.dto.data;



public record AutoSyncConfigDto(
    Long id,
    Long indexInfoId,
    String indexClassification,
    Boolean enabled
) {}
