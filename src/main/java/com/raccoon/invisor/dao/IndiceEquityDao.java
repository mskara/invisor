package com.raccoon.invisor.dao;

import com.raccoon.invisor.model.IndiceEquity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndiceEquityDao extends JpaRepository<IndiceEquity, Long> {

    List<IndiceEquity> findByEquityId(Integer equityId);
    List<IndiceEquity> findByIndiceId(Integer indiceId);


}
