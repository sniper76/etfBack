package com.etf.rest.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KrxDataVO {
	/*
	 * {"response":
	 * {
	 * "header":{"resultCode":"00","resultMsg":"NORMAL SERVICE."},
	 * "body":{"numOfRows":10,"pageNo":1,"totalCount":1,
	 * "items":{"item":[{"basDt":"20220713","srtnCd":"091700","isinCd":"KR7091700005",
	 * "itmsNm":"파트론","mrktCtg":"KOSDAQ","clpr":"8080","vs":"30","fltRt":".37",
	 * "mkp":"8040","hipr":"8110","lopr":"7850","trqu":"580218","trPrc":"4646143740",
	 * "lstgStCnt":"58918214","mrktTotAmt":"476059169120"}]
	 * }
	 * }
	 * }
	 * }
	 */
	private String serviceKey;
	private String resultType;
	private String basDt;
	private String likeSrtnCd;
}
