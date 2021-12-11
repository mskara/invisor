package com.raccoon.invisor.dao;

import com.raccoon.invisor.model.Indice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndiceDao extends JpaRepository<Indice, Long> {

    Indice findByIndiceId(Integer indiceId);

}
