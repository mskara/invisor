package com.raccoon.invisor.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
public class HistoricalData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "instrument_id")
    private Integer instrumentId;

    @Column(name = "date_time")
    private Date date;

    @Column(name = "price_open")
    private BigDecimal priceOpen;

    @Column(name = "price_close")
    private BigDecimal priceClose;

    @Column(name = "price_high")
    private BigDecimal priceHigh;

    @Column(name = "price_low")
    private BigDecimal priceLow;

    @Column(name = "volume")
    private Long volume;
}
