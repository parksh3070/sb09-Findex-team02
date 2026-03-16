package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.entity.base.BaseEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.Id;

@Entity
@Table(name = "auto_sync_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoSyncConfig {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auto_sync_seq")
  @SequenceGenerator(
      name = "auto_sync_seq",
      sequenceName = "auto_sync_sequence",
      allocationSize = 50
  )
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private IndexInfo indexInfo;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled = false;

  @Column(name = "last_sync_at")
  private LocalDate lastSyncAt;

  public AutoSyncConfig(IndexInfo indexInfo){
    this.indexInfo = indexInfo;
  }

  public void updateEnabled(boolean status) {
    this.enabled = status;
  }

  public void updateLastSyncAt() {
    this.lastSyncAt = LocalDate.now();
  }

}
