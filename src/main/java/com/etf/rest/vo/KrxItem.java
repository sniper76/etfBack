package com.etf.rest.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KrxItem implements Comparable<KrxItem> {
	private String ISU_SRT_CD;//풀코드
	private String ISU_CD;//종목코드
	private String ISU_ABBRV;//종목명
	private String MKT_ID;//STK KSQ
	private String MKT_NM;//코스피 코스닥
	private String MKTCAP;//시총
	private double DIV_AVG;//배당평균
	private int DIV_CNT;//배당횟수
	private String CLPR_20200319;
	private String LOPR_20200319;
	private String HIPR_20200319;
	private String  MKP_20200319;
	private String CLPR_20220713;
	private String LOPR_20220713;
	private String HIPR_20220713;
	private String  MKP_20220713;
	private String CLPR_DATE;
	private String LOPR_DATE;
	private String HIPR_DATE;
	private String  MKP_DATE;
	private String PRICE_RECENT;

	@Override
	public int compareTo(KrxItem o) {
//		if (this.DIV_AVG < o.getDIV_AVG()) {
//	        return 1;
//	    } else if (this.DIV_AVG > o.getDIV_AVG()) {
//	        return -1;
//	    }
		if (this.DIV_CNT < o.getDIV_CNT()) {
			return 1;
		} else if (this.DIV_CNT > o.getDIV_CNT()) {
			return -1;
		}
//		return Comparator.comparingInt(KrxItem::getDIV_CNT)
//        .thenComparingDouble(KrxItem::getDIV_AVG)
//        .compare(this, o);
		return 0;
	}

}
