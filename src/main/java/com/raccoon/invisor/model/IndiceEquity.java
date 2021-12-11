package com.raccoon.invisor.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class IndiceEquity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "inv_indice_id")
    private Integer indiceId;

    @Column(name = "inv_equity_id")
    private Integer equityId;

    public IndiceEquity(Integer indiceId, Integer equityId) {
        this.indiceId = indiceId;
        this.equityId = equityId;
    }

    public IndiceEquity() {

    }
}
