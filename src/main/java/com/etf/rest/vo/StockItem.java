package com.etf.rest.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StockItem implements Comparable<StockItem> {
	private String stockCd;
	private String stockNm;
	private int price20200108;
	private int price20200319;
	private int price20220713;
	private int price20220930;
	private int priceOneDayBefore;
	
	@Override
    public int compareTo(StockItem item) {
//        if (item.priceOneDayBefore - item.price20220930 > priceOneDayBefore - price20220930) {
//            return -1;
//        } else if (item.priceOneDayBefore - item.price20220930 < priceOneDayBefore - price20220930) {
//            return 1;
//        } else if (item.priceOneDayBefore <= 0 || item.price20220930 <= 0) {
//        	return -1;
//        }
		if (item.priceOneDayBefore <= 0 || item.price20220930 <= 0) {
	    	return -1;
	    }
        return (priceOneDayBefore - price20220930) - (item.getPriceOneDayBefore() - item.getPrice20220930());
    }
}
