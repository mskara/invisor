package com.raccoon.invisor.service;

import com.raccoon.invisor.client.InvestingClient;
import com.raccoon.invisor.dao.EquityDao;
import com.raccoon.invisor.dao.HistoricalDataDao;
import com.raccoon.invisor.dao.IndiceDao;
import com.raccoon.invisor.dao.IndiceEquityDao;
import com.raccoon.invisor.model.Equity;
import com.raccoon.invisor.model.HistoricalData;
import com.raccoon.invisor.model.Indice;
import com.raccoon.invisor.model.IndiceEquity;
import com.raccoon.invisor.utils.DateUtils;
import com.raccoon.invisor.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Task {

    @Autowired
    EquityDao ed;

    @Autowired
    IndiceDao id;

    @Autowired
    IndiceEquityDao ied;

    @Autowired
    HistoricalDataDao hdd;

    @Autowired
    InvestingClient ic;

    Logger logger = LoggerFactory.getLogger(Task.class);
    final Date today = new Date();
    final Date refDate = new GregorianCalendar(2019, Calendar.JANUARY, 1).getTime();

    @Transactional
    public void updateInstruments() {

        id.deleteAll();
        ied.deleteAll();
        ed.deleteAll();

        logger.info("Old indice and equity lists are cleared!");

        List<Indice> indiceList = ic.getIndiceList();
        List<Equity> equityList = ic.getEquityList();
        List<HistoricalData> historicalDataList = new ArrayList<>();

        logger.info("Indice and equity lists have recieved!");

        // indice-equity mapping list from list of indice object
        List<IndiceEquity> indiceEquityList = indiceList.stream()
                .flatMap(i -> i.getEquities().stream().map(e -> new IndiceEquity(i.getIndiceId(), e)))
                .collect(Collectors.toList());

        id.saveAll(indiceList);
        ed.saveAll(equityList);
        ied.saveAll(indiceEquityList);

        logger.info(String.format("%d indices, %d equities, %d indice-equity maps are inserted to DB!", indiceList.size(), equityList.size(), indiceEquityList.size()));

        //get all instrument ids from indice and equity lists instrument
        List<Integer> instrumentIdList = Stream.concat(
                indiceList.stream().map(Indice::getIndiceId),
                equityList.stream().map(Equity::getEquityId))
                .collect(Collectors.toList());

        //filter old instrument ids
        List<Integer> oldInstrumentIdList = hdd.findInstrumentIdList().stream()
                .filter(t -> !instrumentIdList.contains(t))
                .collect(Collectors.toList());

        hdd.deleteByInstrumentIdIn(oldInstrumentIdList);
        hdd.deleteByDate(today);

        logger.info("Historical datas of the unused instruments have been cleared from DB!");

        //fetch data that between last date in db and today for each instrument.
        Map<Integer, Date> maxDateInstrumentIdMap = hdd.getInstrumentIdLastDateMap();
        instrumentIdList.stream()
                .collect(Collectors.toMap(t -> t, t -> Utils.nvl(maxDateInstrumentIdMap.get(t), refDate))).entrySet()
                .stream()
                .filter(map -> !DateUtils.isSameDay(map.getValue(), today))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach((k, v) -> historicalDataList.addAll(ic.getHistoricalDatas(k, v, today)));

        logger.info(String.format("%d historical datas are recieved!", historicalDataList.size()));

        hdd.saveAll(historicalDataList);

        logger.info("Historical datas have been inserted!");

    }

}
