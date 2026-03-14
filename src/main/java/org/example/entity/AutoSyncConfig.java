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
@Table(name = "auto_sync_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoSyncConfig extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled = false;

  @Column(name = "last_sync_at")
  private Instant lastSyncAt;

  public AutoSyncConfig(IndexInfo indexInfo){
    this.indexInfo = indexInfo;
  }

  public void updateEnabled(boolean status) {
    this.enabled = status;
  }

  public void updateLastSyncAt() {
    this.lastSyncAt = Instant.now();
  }

}
