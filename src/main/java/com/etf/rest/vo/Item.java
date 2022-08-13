package com.etf.rest.vo;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Item implements Comparable<Item> {
	private String TRD_DT;
	private BigDecimal AGMT_STK_CNT;
	private String STK_NM_KOR;
	private BigDecimal ETF_WEIGHT;
	private int STK_NM_CNT;

//	public String getTRD_DT() {
//		return TRD_DT;
//	}
//	public void setTRD_DT(String tRD_DT) {
//		TRD_DT = tRD_DT;
//	}
//	public BigDecimal getAGMT_STK_CNT() {
//		return AGMT_STK_CNT;
//	}
//	public void setAGMT_STK_CNT(BigDecimal aGMT_STK_CNT) {
//		AGMT_STK_CNT = aGMT_STK_CNT;
//	}
//	public String getSTK_NM_KOR() {
//		return STK_NM_KOR;
//	}
//	public void setSTK_NM_KOR(String sTK_NM_KOR) {
//		STK_NM_KOR = sTK_NM_KOR;
//	}
//	public BigDecimal getETF_WEIGHT() {
//		return ETF_WEIGHT;
//	}
//	public void setETF_WEIGHT(BigDecimal eTF_WEIGHT) {
//		ETF_WEIGHT = eTF_WEIGHT;
//	}
//	public int getSTK_NM_CNT() {
//		return STK_NM_CNT;
//	}
//	public void setSTK_NM_CNT(int sTK_NM_CNT) {
//		STK_NM_CNT = sTK_NM_CNT;
//	}
//
//	@Override
//	public String toString() {
//		return "Item [TRD_DT=" + TRD_DT + ", AGMT_STK_CNT=" + AGMT_STK_CNT + ", STK_NM_KOR=" + STK_NM_KOR
//				+ ", ETF_WEIGHT=" + ETF_WEIGHT + ", STK_NM_CNT=" + STK_NM_CNT + "]";
//	}

	@Override
	public int compareTo(Item o) {
		if (this.STK_NM_CNT < o.getSTK_NM_CNT()) {
	        return 1;
	    } else if (this.STK_NM_CNT > o.getSTK_NM_CNT()) {
	        return -1;
	    }
		return 0;
	}

}
