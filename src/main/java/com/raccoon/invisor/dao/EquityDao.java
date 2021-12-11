package com.raccoon.invisor.dao;

import com.raccoon.invisor.model.Equity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquityDao extends JpaRepository<Equity, Long> {

    Equity findByEquityId(Integer equityId);

    Equity findByEquitySymbol(String equitySymbol);


}
