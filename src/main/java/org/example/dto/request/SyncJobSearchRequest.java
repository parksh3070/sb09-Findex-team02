package org.example.dto.request;

import java.time.LocalDate;
import org.example.entity.type.JobType;
import org.example.entity.type.StatusType;

public record SyncJobSearchRequest(

    JobType jobType,
    Long indexInfoId,
    LocalDate baseDateFrom,
    LocalDate baseDateTo,
    String worker,
    StatusType status,
    LocalDate jobTimeFrom,
    LocalDate jobTimeTo,

    Long idAfter,
    String cursor,

    String sortField,
    String sortDirection,

    Integer size

) {}