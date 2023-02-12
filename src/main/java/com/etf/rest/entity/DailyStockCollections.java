package com.etf.rest.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "daily_stock_infos")
@ToString
public class DailyStockCollections {

    @Id
    private String dateString;
    private String dataStk;
    private String dataKsq;

    @Builder
    public DailyStockCollections(String dateString, String dataStk, String dataKsq) {
        this.dateString = dateString;
        this.dataStk = dataStk;
        this.dataKsq = dataKsq;
    }
}