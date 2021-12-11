package com.raccoon.invisor.rest;

import com.raccoon.invisor.client.InvestingClient;
import com.raccoon.invisor.dao.EquityDao;
import com.raccoon.invisor.dao.HistoricalDataDao;
import com.raccoon.invisor.dao.IndiceDao;
import com.raccoon.invisor.model.Equity;
import com.raccoon.invisor.model.HistoricalData;
import com.raccoon.invisor.model.Indice;
import com.raccoon.invisor.service.Task;
import com.raccoon.invisor.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@RestController
public class Hello {

    @Autowired
    EquityDao ed;

    @Autowired
    IndiceDao id;

    @Autowired
    HistoricalDataDao hd;

    @Autowired
    InvestingClient ic;

    @Autowired
    Task task;

    @GetMapping("/test")
    public Indice test() {
        return id.findByIndiceId(19155);
    }

    //client için
    @GetMapping("/webEquities")
    public List<Equity> getEquities() {
        return ic.getEquityList();
    }

    @GetMapping("/webIndices")
    public List<Indice> getIndices() {
        return ic.getIndiceList();
    }

    @GetMapping("/hd")
    public List<HistoricalData> getHD() {
        task.updateInstruments();
        return null;
    }

}

