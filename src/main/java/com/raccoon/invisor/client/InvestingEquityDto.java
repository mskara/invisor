package com.raccoon.invisor.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class InvestingEquityDto {

    private Integer totalCount;
    private List<Hit> hits;

    @Data
    static class Hit {
        private Integer pair_ID;
        private ViewData viewData;
    }

    @Data
    static class ViewData {
        private String symbol;
        private String link;
        private String name;

    }
}