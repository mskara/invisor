package com.raccoon.invisor.dao;

import com.raccoon.invisor.model.HistoricalData;
import com.raccoon.invisor.model.HistoricalDataLastDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface HistoricalDataDao extends JpaRepository<HistoricalData, Long> {

    List<HistoricalData> findByInstrumentId(Integer instrumentId);

    @Query(value = "select distinct instrument_id FROM historical_data", nativeQuery = true)
    List<Integer> findInstrumentIdList();

    @Query(value = "select a.instrument_id as istrumentId, max(date_time) + INTERVAL 1 DAY as lastDate from investing_db.historical_data a group by a.instrument_id",
            nativeQuery = true)
    List<HistoricalDataLastDate> findLastDateGroupByInstrumentId();

    @Query(value = "select max(update_time) from investing_db.historical_data a", nativeQuery = true)
    Date findLastUpdateTime();

    void deleteByInstrumentIdIn(List<Integer> instrumentIdList);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteByDate(Date date);

    default Map<Integer, Date> getInstrumentIdLastDateMap() {
        return findLastDateGroupByInstrumentId().stream()
                .collect(Collectors.toMap(HistoricalDataLastDate::getIstrumentId, HistoricalDataLastDate::getLastDate));
    }

}
