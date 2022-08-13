package com.etf.rest.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AllocationPrice {
	private String avgPrice;
	private List<String> priceList;
}
