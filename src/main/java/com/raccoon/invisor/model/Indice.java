package com.raccoon.invisor.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Data
@Entity
public class Indice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "inv_indice_id")
    private Integer indiceId;

    @Column(name = "inv_indice_name")
    private String indiceName;

    @Column(name = "inv_indice_href")
    private String indiceHref;

    @Column(name = "inv_indice_type")
    private Integer indiceType;

    @Transient
    private Set<Integer> equities;

}
