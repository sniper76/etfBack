package com.etf.rest.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KrxReqVO {
	private String serviceKey;
	private String resultType;
	private String basDt;
	private String likeSrtnCd;
}
