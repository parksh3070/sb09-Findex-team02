package org.example.dto.response;

import java.util.List;

public record CursorPageResponseIndexInfoDto<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    Integer size,
    Long totalElement,
    Boolean hasNext
) {}
