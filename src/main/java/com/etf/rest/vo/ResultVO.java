package com.etf.rest.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResultVO {
	private List<EtfItem> etfList;
	private List<Item> stockList;
	private List<KrxItem> krxList;
}
