package com.raccoon.invisor.client;

import com.raccoon.invisor.model.Equity;
import com.raccoon.invisor.model.HistoricalData;
import com.raccoon.invisor.model.Indice;

import java.util.Date;
import java.util.List;

public interface IClient {
    public List<Equity> getEquityList();

    public List<Indice> getIndiceList();

    public List<HistoricalData> getHistoricalDatas(Integer instrumentId, Date startDate, Date endDate);
}
