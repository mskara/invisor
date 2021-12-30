package com.raccoon.invisor.client;

import com.raccoon.invisor.model.Equity;
import com.raccoon.invisor.model.HistoricalData;
import com.raccoon.invisor.model.Indice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;


@Component
public class InvestingClient implements IClient {

    //userAgent might be changed every day, while the cookie can be changed every 30 min (Cloudflare)
    private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36";
    private final String cookie = "cf_clearance=KkZqPhB8oHj5_Xv7TEEZMfrMF2hhlQ6ddVWKbD725OM-1639221170-0-150    ;";

    @Override
    public List<Equity> getEquityList() {

        //mandatory request header parameters
        String userAgent = this.userAgent;
        String cookie = this.cookie;
        String url = "https://tr.investing.com/stock-screener/Service/SearchStocks";

        List<Equity> equityList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        //to convert even if the response is text/html
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_HTML));
        restTemplate.getMessageConverters().add(converter);

        //to create request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        headers.set(HttpHeaders.COOKIE, cookie);
        headers.set("X-Requested-With", "XMLHttpRequest");

        //to create request body with parameters
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("country[]", "63");
        body.add("equityType", "ORD, REIT");
        body.add("order[col]", "viewData.symbol");
        body.add("order[dir]", "a");

        int pageNumber = 1;
        int maxPageNumber = 1;
        int paginationLimit = 50;

        while (pageNumber <= maxPageNumber) {

            //add pn that request parameter for pagination
            body.put("pn", Collections.singletonList(String.valueOf(pageNumber)));

            //make request and assign the response to object
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            InvestingEquityDto response = restTemplate.postForObject(url, request, InvestingEquityDto.class);

            //parse response and assign the model object
            for (InvestingEquityDto.Hit hit : response.getHits()) {
                Equity equity = new Equity();
                equity.setEquityId(hit.getPair_ID());
                equity.setEquitySymbol(hit.getViewData().getSymbol());
                equity.setEquityName(hit.getViewData().getName());
                equity.setEquityHref(hit.getViewData().getLink());

                equityList.add(equity);
            }
            maxPageNumber = response.getTotalCount() / paginationLimit + 1;
            pageNumber++;
        }
        return equityList;
    }

    @Override
    public List<Indice> getIndiceList() {

        //mandatory request header parameters
        String userAgent = this.userAgent;
        String cookie = this.cookie;
        String url = "https://tr.investing.com/indices/turkey-indices";

        List<Indice> indiceList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        //to create request headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        headers.set(HttpHeaders.COOKIE, cookie);
        headers.set("X-Requested-With", "XMLHttpRequest");

        //to create URI with request parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("majorIndices", "on")
                .queryParam("primarySectors", "on")
                .queryParam("additionalIndices", "on");

        //make get request
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        //parse HTML document to object with Jsoup
        Document doc = Jsoup.parse(response.getBody());
        Elements rows = doc.select("#cr1>tbody>tr>td.elp");

        for (Element row : rows) {
            Indice indice = new Indice();
            indice.setIndiceId(Integer.valueOf(row.select("span").attr("data-id")));
            indice.setIndiceName(row.select("span").attr("data-name"));
            indice.setIndiceHref(row.select("a").attr("href"));

            //get indice-equity relation from investing.com with another request
            indice.setEquities(getIndiceEquityMap(indice.getIndiceHref()));

            indiceList.add(indice);
        }

        return indiceList;
    }

    private Set<Integer> getIndiceEquityMap(String indiceHref) {

        //mandatory request header parameters
        String userAgent = this.userAgent;
        String cookie = this.cookie;
        String url = "https://tr.investing.com";
        String urlSuffix = "-components";

        Set<Integer> equityIdList = new HashSet<>();
        RestTemplate restTemplate = new RestTemplate();

        //to create request headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        headers.set(HttpHeaders.COOKIE, cookie);
        headers.set("X-Requested-With", "XMLHttpRequest");

        //to create URI with request parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).path(indiceHref + urlSuffix);

        //make get request
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        //parse HTML document to object with Jsoup
        Document doc = Jsoup.parse(response.getBody());
        Elements rows = doc.select("#cr1>tbody>tr>td.elp>span");

        rows.forEach(row -> equityIdList.add(Integer.valueOf(row.attr("data-id"))));
        return equityIdList;
    }

    @Override
    public List<HistoricalData> getHistoricalDatas(Integer instrumentId, Date startDate, Date endDate) {

        //converter lambdas
        Function<String, BigDecimal> bdConverter = a -> new BigDecimal(a.replace(".", "").replace(",", "."));
        Function<String, Date> dateConverter = a -> new Date(Long.parseLong(a) * 1000);
        Function<Date, String> dateFormatter = a -> new SimpleDateFormat("dd/MM/yyyy").format(a);

        //mandatory request header parameters
        String userAgent = this.userAgent;
        String cookie = this.cookie;
        String url = "https://tr.investing.com/instruments/HistoricalDataAjax";

        List<HistoricalData> historicalDataList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        //to create request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        headers.set(HttpHeaders.COOKIE, cookie);
        headers.set("X-Requested-With", "XMLHttpRequest");

        //to create request body with parameters
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("curr_id", instrumentId.toString());
        body.add("st_date", dateFormatter.apply(startDate));
        body.add("end_date", dateFormatter.apply(endDate));

        //make request and assign the response to object
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        //parse HTML document to object with Jsoup
        Document doc = Jsoup.parse(response.getBody());
        Elements rows = doc.select("#curr_table>tbody>tr");

        for (Element row : rows) {
            List<String> colList = row.select("td").eachAttr("data-real-value");

            if (!colList.isEmpty()) {
                HistoricalData historicalData = new HistoricalData();
                //0:date, 1:priceClose, 2:priceOpen, 3:priceHigh, 4:priceLow, 5:volume
                historicalData.setInstrumentId(instrumentId);
                historicalData.setDate(dateConverter.apply(colList.get(0)));
                historicalData.setPriceClose(bdConverter.apply(colList.get(1)));
                historicalData.setPriceOpen(bdConverter.apply(colList.get(2)));
                historicalData.setPriceHigh(bdConverter.apply(colList.get(3)));
                historicalData.setPriceLow(bdConverter.apply(colList.get(4)));
                historicalData.setVolume(Long.valueOf(colList.get(5)));
                historicalDataList.add(historicalData);
            }
        }

        return historicalDataList;
    }

}
