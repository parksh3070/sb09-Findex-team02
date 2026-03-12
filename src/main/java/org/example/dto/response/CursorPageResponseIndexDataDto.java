package org.example.dto.response;

import java.util.List;

public record CursorPageResponseIndexDataDto<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {

}
