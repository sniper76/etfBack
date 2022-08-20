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
@Document(collection = "stock_infos")
@ToString
public class StockInfoCollections {

    @Id
    private String mktId;//STK 코스피 KSQ 코스닥
    private String info;

    @Builder
    public StockInfoCollections(String mktId, String info) {
        this.mktId = mktId;
        this.info = info;
    }
}