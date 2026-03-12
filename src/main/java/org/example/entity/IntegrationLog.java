package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.entity.base.BaseEntity;
import org.example.entity.type.JobType;
import org.example.entity.type.StatusType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "integration_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntegrationLog extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_id", nullable = false)
  private IndexInfo indexInfo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "data_id")
  private IndexData indexData;

  @Column(name = "target_date")
  private Instant targetDate;

  @Column(name = "worker", length = 255)
  private String worker;

  @Column(name = "worked_at")
  private Instant workedAt;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", columnDefinition = "status_enum")
  private StatusType status;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "job_type", columnDefinition = "job_type_enum")
  private JobType jobType;

  public IntegrationLog(IndexInfo indexInfo, JobType jobType, StatusType status) {
    this.indexInfo = indexInfo;
    this.jobType = jobType;
    this.status = status;
  }

  // 생성자 파라미터 너무 길어져서 분리함
  // 세부 연동 작업 정보 설정
  public void setIntegrationLogDetails(IndexData indexData, Instant targetDate, String worker, Instant workedAt) {
    this.indexData = indexData;
    this.targetDate = targetDate;
    this.worker = worker;
    this.workedAt = workedAt;
  }
}
