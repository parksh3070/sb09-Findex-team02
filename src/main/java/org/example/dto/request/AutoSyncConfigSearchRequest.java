package org.example.dto.request;

public record AutoSyncConfigSearchRequest(
    Long indexInfoId,
    Boolean enabled,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {
  // 기본값을 설정하기 위한 컴팩트 생성자
  public AutoSyncConfigSearchRequest {
    if (sortField == null) sortField = "indexInfo.indexName";
    if (sortDirection == null) sortDirection = "asc";
    if (size == null) size = 10;
  }
}
