package org.example.repository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.example.dto.request.SyncJobSearchRequest;
import org.example.entity.IntegrationLog;
import org.springframework.data.jpa.domain.Specification;

public class SyncJobSpec {
  private SyncJobSpec() {}

  public static Specification<IntegrationLog> of(SyncJobSearchRequest request) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (request.jobType() != null) {
        predicates.add(cb.equal(root.get("jobType"), request.jobType()));
      }
      if (request.indexInfoId() != null) {
        predicates.add(cb.equal(root.get("indexInfo").get("id"), request.indexInfoId()));
      }
      if (request.baseDateFrom() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("targetDate"), request.baseDateFrom()));
      }
      if (request.baseDateTo() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("targetDate"), request.baseDateTo()));
      }
      if (request.worker() != null && !request.worker().isBlank()) {
        predicates.add(cb.equal(root.get("worker"), request.worker()));
      }
      if (request.status() != null) {
        predicates.add(cb.equal(root.get("result"), request.status()));
      }
      if (request.jobTimeFrom() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("workedAt"), request.jobTimeFrom()));
      }
      if (request.jobTimeTo() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("workedAt"), request.jobTimeTo()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
