package com.etf.rest.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EtfResponse {

	private String resultCode;
	private EtfItemList result;
//	public String getResultCode() {
//		return resultCode;
//	}
//	public void setResultCode(String resultCode) {
//		this.resultCode = resultCode;
//	}
//	public EtfItemList getResult() {
//		return result;
//	}
//	public void setResult(EtfItemList result) {
//		this.result = result;
//	}
//
//	@Override
//	public String toString() {
//		return "EtfResponse [resultCode=" + resultCode + ", result=" + result + "]";
//	}
}
