package org.example.entity;

import org.example.entity.type.SourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.entity.base.BaseEntity;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "index_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexData extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "base_date", nullable = false)
  private Instant baseDate;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "source_type", columnDefinition = "source_type_enum")
  private SourceType sourceType;

  @Column(name = "open_price", precision = 19, scale = 4)
  private BigDecimal openPrice;

  @Column(name = "close_price", precision = 19, scale = 4)
  private BigDecimal closePrice;

  @Column(name = "high_price", precision = 19, scale = 4)
  private BigDecimal highPrice;

  @Column(name = "low_price", precision = 19, scale = 4)
  private BigDecimal lowPrice;

  @Column(name = "price_diff", precision = 19, scale = 4)
  private BigDecimal priceDiff;

  @Column(name = "fluctuation_rate", precision = 19, scale = 4)
  private BigDecimal fluctuationRate;

  @Column(name = "trade_volume")
  private Long tradeVolume;

  @Column(name = "trade_amount")
  private Long tradeAmount;

  @Column(name = "market_cap")
  private Long marketCap;

  public IndexData(IndexInfo indexInfo, Instant baseDate, SourceType sourceType) {
    super();
    this.indexInfo = indexInfo;
    this.baseDate = baseDate;
    this.sourceType = sourceType;
  }

  // 생성자에 파라미터가 너무 길어져서 따로 분리함
  public void setPrices(BigDecimal open, BigDecimal close, BigDecimal high, BigDecimal low) {
    this.openPrice = open;
    this.closePrice = close;
    this.highPrice = high;
    this.lowPrice = low;
  }

  public void setFluctuationInfo(BigDecimal diff, BigDecimal rate) {
    this.priceDiff = diff;
    this.fluctuationRate = rate;
  }

  public void setMarketData(Long volume, Long amount, Long cap) {
    this.tradeVolume = volume;
    this.tradeAmount = amount;
    this.marketCap = cap;
  }
}
