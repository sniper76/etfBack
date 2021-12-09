package com.etf.rest.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EtfItemList {

	private List<EtfItem> etfItemList;

//	public List<EtfItem> getEtfItemList() {
//		return etfItemList;
//	}
//
//	public void setEtfItemList(List<EtfItem> etfItemList) {
//		this.etfItemList = etfItemList;
//	}
//
//	@Override
//	public String toString() {
//		return "EtfItemList [etfItemList=" + etfItemList + "]";
//	}
}
