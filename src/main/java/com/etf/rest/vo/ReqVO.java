package com.etf.rest.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReqVO {
	private String result;
	private String data;
	private String searchText;
	private String searchRate;
}
