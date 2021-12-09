package com.etf.rest.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EtfItem {
/**
"itemcode":"069500","etfTabCode":1,"itemname":"KODEX 200","nowVal":38825,"risefall":"5","changeVal":-635,"changeRate":-1.61,"nav":38876.0,"threeMonthEarnRate":-9.5368,"quant":3408700,"amonut":133339,"marketSum":53734
"itemcode":"153130","etfTabCode":6,"itemname":"KODEX 단기채권","nowVal":102925,"risefall":"5","changeVal":-20,"changeRate":-0.02,"nav":102930.0,"threeMonthEarnRate":0.0729,"quant":15868,"amonut":1633,"marketSum":23327
"itemcode":"252670","etfTabCode":3,"itemname":"KODEX 200선물인버스2X","nowVal":2300,"risefall":"2","changeVal":70,"changeRate":3.14,"nav":2308.0,"threeMonthEarnRate":19.7916,"quant":178898061,"amonut":404392,"marketSum":22545
"itemcode":"371460","etfTabCode":4,"itemname":"TIGER 차이나전기차SOLACTIVE","nowVal":19470,"risefall":"2","changeVal":130,"changeRate":0.67,"nav":null,"threeMonthEarnRate":17.3598,"quant":6445462,"amonut":124347,"marketSum":21892
"itemcode":"102110","etfTabCode":1,"itemname":"TIGER 200","nowVal":38885,"risefall":"5","changeVal":-605,"changeRate":-1.53,"nav":38894.0,"threeMonthEarnRate":-9.3315,"quant":570595,"amonut":22270,"marketSum":20920
"itemcode":"214980","etfTabCode":6,"itemname":"KODEX 단기채권PLUS","nowVal":103210,"risefall":"5","changeVal":-15,"changeRate":-0.01,"nav":103216.0,"threeMonthEarnRate":0.063,"quant":2881635,"amonut":297429,"marketSum":19604
"itemcode":"102780","etfTabCode":2,"itemname":"KODEX 삼성그룹","nowVal":9790,"risefall":"5","changeVal":-180,"changeRate":-1.81,"nav":9786.0,"threeMonthEarnRate":-7.9888,"quant":138258,"amonut":1364,"marketSum":17465
"itemcode":"122630","etfTabCode":3,"itemname":"KODEX 레버리지","nowVal":22935,"risefall":"5","changeVal":-740,"changeRate":-3.13,"nav":22937.0,"threeMonthEarnRate":-18.2936,"quant":24310507,"amonut":566235,"marketSum":16559
"itemcode":"157450","etfTabCode":6,"itemname":"TIGER 단기통안채","nowVal":100375,"risefall":"3","changeVal":0,"changeRate":0.0,"nav":100366.0,"threeMonthEarnRate":0.1296,"quant":2417,"amonut":242,"marketSum":15699
"itemcode":"133690","etfTabCode":4,"itemname":"TIGER 미국나스닥100","nowVal":81910,"risefall":"3","changeVal":0,"changeRate":0.0,"nav":null,"threeMonthEarnRate":6.3683,"quant":122499,"amonut":10049,"marketSum":15071
 */
	private String itemcode;
	private int etfTabCode;
	private String itemname;
	private int nowVal;
	private String risefall;
	private int changeVal;
	private double changeRate;
//	private int nav;
	private double threeMonthEarnRate;
	private int quant;
	private int amonut;
	private int marketSum;
//	public String getItemcode() {
//		return itemcode;
//	}
//	public void setItemcode(String itemcode) {
//		this.itemcode = itemcode;
//	}
//	public int getEtfTabCode() {
//		return etfTabCode;
//	}
//	public void setEtfTabCode(int etfTabCode) {
//		this.etfTabCode = etfTabCode;
//	}
//	public String getItemname() {
//		return itemname;
//	}
//	public void setItemname(String itemname) {
//		this.itemname = itemname;
//	}
//	public int getNowVal() {
//		return nowVal;
//	}
//	public void setNowVal(int nowVal) {
//		this.nowVal = nowVal;
//	}
//	public String getRisefall() {
//		return risefall;
//	}
//	public void setRisefall(String risefall) {
//		this.risefall = risefall;
//	}
//	public int getChangeVal() {
//		return changeVal;
//	}
//	public void setChangeVal(int changeVal) {
//		this.changeVal = changeVal;
//	}
//	public double getChangeRate() {
//		return changeRate;
//	}
//	public void setChangeRate(double changeRate) {
//		this.changeRate = changeRate;
//	}
//	public double getThreeMonthEarnRate() {
//		return threeMonthEarnRate;
//	}
//	public void setThreeMonthEarnRate(double threeMonthEarnRate) {
//		this.threeMonthEarnRate = threeMonthEarnRate;
//	}
//	public int getQuant() {
//		return quant;
//	}
//	public void setQuant(int quant) {
//		this.quant = quant;
//	}
//	public int getAmonut() {
//		return amonut;
//	}
//	public void setAmonut(int amonut) {
//		this.amonut = amonut;
//	}
//	public int getMarketSum() {
//		return marketSum;
//	}
//	public void setMarketSum(int marketSum) {
//		this.marketSum = marketSum;
//	}
//	
//	@Override
//	public String toString() {
//		return "EtfItem [itemcode=" + itemcode + ", etfTabCode=" + etfTabCode + ", itemname=" + itemname + ", nowVal="
//				+ nowVal + ", risefall=" + risefall + ", changeVal=" + changeVal + ", changeRate=" + changeRate
//				+ ", threeMonthEarnRate=" + threeMonthEarnRate + ", quant=" + quant + ", amonut=" + amonut
//				+ ", marketSum=" + marketSum + "]";
//	}	
}
