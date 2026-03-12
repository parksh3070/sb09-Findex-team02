package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.entity.base.BaseEntity;

@Entity
@Table(name = "integration_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntegrationConfig extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = false;

  @Column(name = "last_sync_at")
  private Instant lastSyncAt;

  public void updateActive(boolean status) {
    this.isActive = status;
  }

  public void updateLastSyncAt() {
    this.lastSyncAt = Instant.now();
  }
}
