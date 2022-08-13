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
@Document(collection = "etf_infos")
@ToString
public class EtfInfoCollections {

    @Id
    private String date;
    private String info;

    @Builder
    public EtfInfoCollections(String date, String info) {
        this.date = date;
        this.info = info;
    }
}