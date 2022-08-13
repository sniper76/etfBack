package com.etf.rest.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "access_logs")
@ToString
public class LogCollections {

	@Transient
    public static final String SEQUENCE_NAME = "access_logs_sequence";

    @Id
    private Long id;
    private String date;
    private String keyword;
    private String address;

    public void setId(Long id) {
    	this.id = id;
    }

    @Builder
    public LogCollections(Long id, String date, String keyword, String address) {
        this.id = id;
        this.date = date;
        this.keyword = keyword;
        this.address = address;
    }
}