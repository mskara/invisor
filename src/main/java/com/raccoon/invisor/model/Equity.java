package com.raccoon.invisor.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
public class Equity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "inv_equity_id")
    private Integer equityId;

    @Column(name = "inv_equity_symbol")
    private String equitySymbol;

    @Column(name = "inv_equity_name")
    private String equityName;

    @Column(name = "inv_equity_href")
    private String equityHref;

}
