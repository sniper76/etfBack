package com.etf.rest.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.etf.rest.vo.KrxItem;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "krx_infos")
@ToString
public class KrxDataCollections {

    @Id
    private String mktId;//STK 코스피 KSQ 코스닥
    private List<KrxItem> stockData;

    @Builder
    public KrxDataCollections(String mktId, List<KrxItem> stockData) {
        this.mktId = mktId;
        this.stockData = stockData;
    }
}