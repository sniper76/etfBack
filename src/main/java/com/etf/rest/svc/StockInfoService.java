package com.etf.rest.svc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.etf.rest.entity.KrxDataCollections;
import com.etf.rest.entity.StockInfoCollections;
import com.etf.rest.repo.KrxDataRepository;
import com.etf.rest.repo.StockRepository;
import com.etf.rest.util.Utils;
import com.etf.rest.vo.AllocationPrice;
import com.etf.rest.vo.Item;
import com.etf.rest.vo.KrxItem;
import com.etf.rest.vo.KrxReqVO;
import com.etf.rest.vo.ReqVO;
import com.etf.rest.vo.ResultVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StockInfoService {
	Logger logger = LoggerFactory.getLogger(StockInfoService.class);

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private KrxDataRepository krxDataRepository;

	@Autowired
	private StockRepository stockRepository;

	public ReqVO getData() {
		logger.debug("test : {}, {}", "한글이~~~", "abc");
		ReqVO vo = new ReqVO();
		vo.setResult("success");
		vo.setData("한글이깨지나?");
		return vo;
	}

	public ResultVO stockData(ReqVO reqVO) {
		ResultVO ls = new ResultVO();
		try {
//			reqVO.getData();//STK 코스피 KSQ 코스닥
			String stockInfoString = searchMongoStockInfo(reqVO.getData());

			if (stockInfoString == null) {
				stockInfoString = getDataPost("POST", reqVO.getData(), Utils.toDate("yyyyMMdd"));

				// insert
				StockInfoCollections entity = StockInfoCollections.builder().mktId(reqVO.getData())
						.info(stockInfoString).build();

				// Repository 버전
				stockRepository.save(entity);
			}

			logger.info("stockInfoString : {}", stockInfoString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}
	
	public ResultVO searchStock(ReqVO reqVO) {
		ResultVO ls = new ResultVO();
		try {
//			reqVO.getData();//STK 코스피 KSQ 코스닥
			String stockInfoString = searchMongoStockInfo(reqVO.getData());
			
			if (stockInfoString == null) {
				stockInfoString = getDataPost("POST", reqVO.getData(), Utils.toDate("yyyyMMdd"));
				
				// insert
				StockInfoCollections entity = StockInfoCollections.builder().mktId(reqVO.getData())
						.info(stockInfoString).build();
				
				// Repository 버전
				stockRepository.save(entity);
			}
			
			logger.info("stockInfoString : {}", stockInfoString);
			
			List<Item> dataList = stockList(stockInfoString);
			ls.setStockList(dataList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	public ResultVO searchKrxData(ReqVO reqVO) {
		ResultVO ls = new ResultVO();
		try {
//			reqVO.getData();//STK 코스피 KSQ 코스닥
			List<KrxItem> dataList = searchMongoFindAllocationPriceRateKrxData(reqVO);
//			if (reqVO.getSearchText() == null) {

//				Collections.sort(dataList, Comparator.comparingInt(KrxItem::getDIV_CNT).reversed());
//				Collections.sort(dataList);
				ls.setKrxList(dataList);
//			} else {
//
////				Collections.sort(, Collections.reverseOrder());
//				ls.setKrxList(dataList);
//
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	public String searchMongoStockInfo(String key) throws Exception {

		StockInfoCollections stock = mongoOperations.findOne(Query.query(Criteria.where("mktId").is(key)),
				StockInfoCollections.class);
		logger.debug("stock : {}", stock);
		if (stock != null) {
			return stock.getInfo();
		}
		return null;
	}

	public List<KrxItem> searchMongoKrxData(String mktId) throws Exception {

		KrxDataCollections stock = mongoOperations.findOne(Query.query(Criteria.where("mktId").is(mktId)),
				KrxDataCollections.class);
		logger.debug("stock : {}", stock);
		if (stock != null) {
			return stock.getStockData();
		}
		return null;
	}
	
	public List<KrxItem> searchMongoFindAllocationPriceRateKrxData(ReqVO vo) throws Exception {
		
		KrxDataCollections stock = mongoOperations.findOne(Query.query(Criteria.where("mktId").is(vo.getData())),
				KrxDataCollections.class);
		logger.debug("stock : {}", stock);
		if (stock != null) {
			Stream<KrxItem> st = stock.getStockData().stream();
			
			if(vo.getSearchRate() != null && !vo.getSearchRate().isEmpty()) {				
				st = st.filter(k -> k.getDIV_AVG() >= Double.parseDouble(vo.getSearchRate()));
			}
			if(vo.getSearchText() != null && !vo.getSearchText().isEmpty()) {
				st = st.filter(i -> i.getISU_ABBRV().contains(vo.getSearchText()));
			}
			if(st == null) {
				return null;
			}
			
			return st.toList();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Item> stockList(String stockStr) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
//		JsonNode actualObj = mapper.readTree(stockStr);
		Map<String, Object> map = mapper.readValue(stockStr, new TypeReference<Map<String, Object>>() {});

//        logger.info("map : {}", map);
		List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("OutBlock_1");
		List<Item> items = new ArrayList<Item>(data.size());
		if (!data.isEmpty()) {
			for (Map<String, Object> map2 : data) {
//				logger.info("map2 : {}", map2);
				Item item = new Item();
				item.setSTK_NM_KOR(map2.get("ISU_ABBRV").toString());
				item.setTRD_DT(map2.get("ISU_SRT_CD").toString());
				items.add(item);
			}
		}
		// 흥아해운 003280
		// {"ISU_SRT_CD":"095570","ISU_CD":"KR7095570008","ISU_ABBRV":"AJ네트웍스",
//		"MKT_NM":"KOSPI","SECT_TP_NM":"","TDD_CLSPRC":"-","FLUC_TP_CD":"3",
//		"CMPPREVDD_PRC":"-","FLUC_RT":"-","TDD_OPNPRC":"-","TDD_HGPRC":"-",
//		"TDD_LWPRC":"-","ACC_TRDVOL":"-","ACC_TRDVAL":"-","MKTCAP":"-",
//		"LIST_SHRS":"46,822,295","MKT_ID":"STK"}
//		logger.info("actualObj : {}", actualObj.size());
		return items;
	}

	public ResultVO fnguide(ReqVO reqVO) {
		ResultVO ls = new ResultVO();
		try {
			// stock info 조회
//			ReqVO reqVO = new ReqVO();
//			reqVO.setData("KSQ");
			ResultVO vo = searchStock(reqVO);

			List<KrxItem> rtnList = null;

			long start = System.currentTimeMillis();

			if (vo != null && vo.getStockList().size() > 0) {
				rtnList = new ArrayList<KrxItem>(vo.getStockList().size());
				for (Item i : vo.getStockList()) {
					KrxItem r = new KrxItem();
					r.setISU_ABBRV(i.getSTK_NM_KOR());
					r.setISU_SRT_CD(i.getTRD_DT());
					r.setMKT_ID(reqVO.getData());

					String text = searchFnGuideSiteHtmlText(i.getTRD_DT());

//					int startIndex = text.lastIndexOf("배당수익률");
//			logger.info("startIndex : {}, text: {}", startIndex, text);

//					text = text.substring(startIndex);
//					text = text.substring(0, text.indexOf("tdbg_b"));
//					logger.info("startIndex : {}, text: {}", startIndex, text);

//				extractAllocationPrice(text);
					AllocationPrice price = extractTableText(text);

					rtnList.add(r);
					if (price == null || price.getAvgPrice() == null) {
						continue;
					}
					r.setDIV_CNT(price.getPriceList().size());
					r.setDIV_AVG(Double.parseDouble(price.getAvgPrice()));

//					break;
					logger.info("krxItem : {}", r);
					Thread.sleep(1000);
				}

				// insert
				KrxDataCollections entity = KrxDataCollections.builder().mktId(reqVO.getData()).stockData(rtnList)
						.build();

				// Repository 버전
				krxDataRepository.save(entity);
			}
			long end = System.currentTimeMillis();

			NumberFormat formatter = new DecimalFormat("#0.00000");

			logger.info(">>>>>>>>>> finish Execution time is {} {}", formatter.format((end - start) / 1000d),
					" seconds");
//			logger.info(">>>>>>>>>> finish Execution time is {} {}", formatter.format((end - start) / 1000d),
//					" seconds");
//			logger.info(">>>>>>>>>> finish Execution time is {} {}", formatter.format((end - start) / 1000d),
//					" seconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	public ReqVO searchToDayPrice(ReqVO reqVO) {
		ReqVO ls = new ReqVO();
		try {
			// stock info 조회
			// reqVO.result = 종목코드
			// reqVO.data = STK, KSQ
			ResultVO vo = searchStock(reqVO);

			List<Item> rtnList = vo.getStockList();
			Item item = rtnList.stream().filter(i -> reqVO.getResult().equals(i.getTRD_DT())).findFirst().orElse(null);

			if (item != null) {
				logger.info("stockCd : {}", item.getTRD_DT());
				String formatStr = "https://finance.naver.com/item/main.naver?code=%s";
//			String formatStr = "https://www.thinkpool.com/item/%s";
//				String htmlText = searchHtmlText(formatStr, item.getTRD_DT());

//				String patternStr = "<strong.*?>(.*?)</strong>";
//					String patternStr = "<span.*?>(.*?)</span>";
				String price = null;
//				price = extractPriceText(htmlText, patternStr);
				
				price = getStockPriceList(formatStr, item.getTRD_DT());

				ls.setResult(price);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}
	
	public String getStockPriceList(String formatStr, String stockCode) {
		
		String url = String.format(formatStr, stockCode);
		logger.info("url : {}", url);
//	    String stockList = "https://finance.naver.com/item/main.naver?code=091700";
	    Connection conn = Jsoup.connect(url);
	    String thead = null;
	    try {
	      Document document = conn.get();
	      thead = getStockHeader(document); // 칼럼명
//	      String tbody = getStockList(document);   // 데이터 리스트
//	      System.out.println(thead);
//	      System.out.println(tbody);

	    } catch (IOException ignored) {
	    }
	    return thead;
	  }

	  public String getStockHeader(Document document) {
	    Elements stockTableBody = document.select("div.today p.no_today em span:not(.blind)");
	    StringBuilder sb = new StringBuilder();
	    for (Element element : stockTableBody) {
	    	logger.info("text : {}", element);
	        sb.append(element.text());
	    }
	    return sb.toString();
	  }

//	  public String getStockList(Document document) {
//	    Elements stockTableBody = document.select("table.type_2 tbody tr");
//	    StringBuilder sb = new StringBuilder();
//	    for (Element element : stockTableBody) {
//	      if (element.attr("onmouseover").isEmpty()) {
//	        continue;
//	      }
//
//	      for (Element td : element.select("td")) {
//	        String text;
//	        if(td.select(".center a").attr("href").isEmpty()){
//	          text = td.text();
//	        }else{
//	          text = "https://finance.naver.com"+td.select(".center a").attr("href");
//	        }
//	        sb.append(text);
//	        sb.append("   ");
//	      }
//	      sb.append(System.getProperty("line.separator")); //줄바꿈
//	    }
//	    return sb.toString();
//	  }

	@SuppressWarnings("unchecked")
	public ResultVO searchKrx(ReqVO reqVO) {
		ResultVO ls = new ResultVO();
		try {

			List<KrxItem> stockList = searchMongoKrxData(reqVO.getData());// mktId

			// stock info 조회
			KrxReqVO krxReqVO = new KrxReqVO();
			krxReqVO.setServiceKey(encodingKey);
			krxReqVO.setResultType("json");
			if(reqVO.getSearchText() == null || reqVO.getSearchText().isEmpty()) {
				reqVO.setSearchText("");
			}
			krxReqVO.setBasDt(reqVO.getSearchText());
			
			String[] dt = {"20200108", "20200319", "20220713", "20220930"};

			long start = System.currentTimeMillis();
			for (KrxItem krxItem : stockList) {

				Map<String, Object> dtMap = new HashMap<String, Object>();
				dtMap.put("CLPR_20200108", "");
				dtMap.put("LOPR_20200108", "");
				dtMap.put("HIPR_20200108", "");
				dtMap.put( "MKP_20200108", "");
				dtMap.put("CLPR_20200319", "");
				dtMap.put("LOPR_20200319", "");
				dtMap.put("HIPR_20200319", "");
				dtMap.put( "MKP_20200319", "");
				dtMap.put("CLPR_20220713", "");
				dtMap.put("LOPR_20220713", "");
				dtMap.put("HIPR_20220713", "");
				dtMap.put( "MKP_20220713", "");
				dtMap.put("CLPR_DATE", "");
				dtMap.put("LOPR_DATE", "");
				dtMap.put("HIPR_DATE", "");
				dtMap.put( "MKP_DATE", "");
				
				krxReqVO.setLikeSrtnCd(krxItem.getISU_SRT_CD());
//			krxReqVO.setLikeSrtnCd("373220");
				try {
					
					for(int k=0; k<dt.length; k++) {
						
						krxReqVO.setBasDt(dt[k]);
						
						String res = getDataKrxAPI(krxReqVO);
						ObjectMapper mapper = new ObjectMapper();
						Map<String, Object> map = mapper.readValue(res, Map.class);

						if (map != null) {
							Map<String, Object> resMap = (Map<String, Object>) map.get("response");
							Map<String, Object> bodyMap = (Map<String, Object>) resMap.get("body");
							Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
							List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemsMap.get("item");

							if (!itemList.isEmpty()) {
								if(k == 2) {
									dtMap.put("CLPR_20220713", String.valueOf(itemList.get(0).get("clpr")));// 종가
									dtMap.put("LOPR_20220713", String.valueOf(itemList.get(0).get("lopr")));// 저가
									dtMap.put("HIPR_20220713", String.valueOf(itemList.get(0).get("hipr")));// 고가
									dtMap.put( "MKP_20220713", String.valueOf(itemList.get(0).get("mkp")));
								}
								else if(k == 1) {
									dtMap.put("CLPR_20200319", String.valueOf(itemList.get(0).get("clpr")));// 종가
									dtMap.put("LOPR_20200319", String.valueOf(itemList.get(0).get("lopr")));// 저가
									dtMap.put("HIPR_20200319", String.valueOf(itemList.get(0).get("hipr")));// 고가
									dtMap.put( "MKP_20200319", String.valueOf(itemList.get(0).get("mkp")));
								}
								else if(k == 0) {
									dtMap.put("CLPR_20200108", String.valueOf(itemList.get(0).get("clpr")));// 종가
									dtMap.put("LOPR_20200108", String.valueOf(itemList.get(0).get("lopr")));// 저가
									dtMap.put("HIPR_20200108", String.valueOf(itemList.get(0).get("hipr")));// 고가
									dtMap.put( "MKP_20200108", String.valueOf(itemList.get(0).get("mkp")));
								}
								else {
									dtMap.put("CLPR_DATE", String.valueOf(itemList.get(0).get("clpr")));// 종가
									dtMap.put("LOPR_DATE", String.valueOf(itemList.get(0).get("lopr")));// 저가
									dtMap.put("HIPR_DATE", String.valueOf(itemList.get(0).get("hipr")));// 고가
									dtMap.put( "MKP_DATE", String.valueOf(itemList.get(0).get("mkp")));
								}
							}
						}
						Thread.sleep(1000);
					}
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setCLPR_20220713(String.valueOf(dtMap.get("CLPR_20220713")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setHIPR_20220713(String.valueOf(dtMap.get("HIPR_20220713")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setLOPR_20220713(String.valueOf(dtMap.get("LOPR_20220713")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setMKP_20220713(String.valueOf(dtMap.get("MKP_20220713")));
					
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setCLPR_20200319(String.valueOf(dtMap.get("CLPR_20200319")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setHIPR_20200319(String.valueOf(dtMap.get("HIPR_20200319")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setLOPR_20200319(String.valueOf(dtMap.get("LOPR_20200319")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setMKP_20200319(String.valueOf(dtMap.get("MKP_20200319")));
					
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setCLPR_20200108(String.valueOf(dtMap.get("CLPR_20200108")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setHIPR_20200108(String.valueOf(dtMap.get("HIPR_20200108")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setLOPR_20200108(String.valueOf(dtMap.get("LOPR_20200108")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setMKP_20200108(String.valueOf(dtMap.get("MKP_20200108")));
					
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setCLPR_DATE(String.valueOf(dtMap.get("CLPR_DATE")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setHIPR_DATE(String.valueOf(dtMap.get("HIPR_DATE")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setLOPR_DATE(String.valueOf(dtMap.get("LOPR_DATE")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD()))
							.findFirst().get().setMKP_DATE(String.valueOf(dtMap.get("MKP_DATE")));
				} catch (Exception e) {
					logger.error("Error : {}, {}", e.getLocalizedMessage(), e.getMessage());
				}
//				break;
			}
			if (!stockList.isEmpty()) {
				logger.info("stockList 0 : {}", stockList.get(0));
				// insert
				KrxDataCollections entity = KrxDataCollections.builder().mktId(reqVO.getData()).stockData(stockList)
						.build();

				// Repository 버전
				krxDataRepository.save(entity);
			}
			long end = System.currentTimeMillis();

			NumberFormat formatter = new DecimalFormat("#0.00000");

			logger.info(">>>>>>>>>> finish Execution time is {}, {}", formatter.format((end - start) / 1000d),
					" seconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}
	
	@SuppressWarnings("unchecked")
	public ResultVO dayData(ReqVO reqVO) {
		ResultVO ls = new ResultVO();
		try {
			
			List<KrxItem> stockList = searchMongoKrxData(reqVO.getData());// mktId
			
			// stock info 조회
			KrxReqVO krxReqVO = new KrxReqVO();
			krxReqVO.setServiceKey(encodingKey);
			krxReqVO.setResultType("json");
			krxReqVO.setBasDt(reqVO.getSearchText());
			
//			String[] dt = {"20200319", "20220713"};
			
			long start = System.currentTimeMillis();
			for (KrxItem krxItem : stockList) {

				Map<String, Object> dtMap = new HashMap<String, Object>();
				dtMap.put("CLPR_DATE", "");
				dtMap.put("LOPR_DATE", "");
				dtMap.put("HIPR_DATE", "");
				dtMap.put("MKP_DATE", "");

				krxReqVO.setLikeSrtnCd(krxItem.getISU_SRT_CD());
				try {
					String res = getDataKrxAPI(krxReqVO);
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> map = mapper.readValue(res, Map.class);

					if (map != null) {
						Map<String, Object> resMap = (Map<String, Object>) map.get("response");
						Map<String, Object> bodyMap = (Map<String, Object>) resMap.get("body");
						Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
						List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemsMap.get("item");

						if (!itemList.isEmpty()) {
							dtMap.put("CLPR_DATE", String.valueOf(itemList.get(0).get("clpr")));// 종가
							dtMap.put("LOPR_DATE", String.valueOf(itemList.get(0).get("lopr")));// 저가
							dtMap.put("HIPR_DATE", String.valueOf(itemList.get(0).get("hipr")));// 고가
							dtMap.put("MKP_DATE", String.valueOf(itemList.get(0).get("mkp")));
						}
					}

					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD())).findFirst().get()
							.setCLPR_DATE(String.valueOf(dtMap.get("CLPR_DATE")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD())).findFirst().get()
							.setHIPR_DATE(String.valueOf(dtMap.get("HIPR_DATE")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD())).findFirst().get()
							.setLOPR_DATE(String.valueOf(dtMap.get("LOPR_DATE")));
					stockList.stream().filter(s -> krxReqVO.getLikeSrtnCd().equals(s.getISU_SRT_CD())).findFirst().get()
							.setMKP_DATE(String.valueOf(dtMap.get("MKP_DATE")));
				} catch (Exception e) {
					logger.error("Error : {}, {}", e.getLocalizedMessage(), e.getMessage());
				}
				Thread.sleep(1000);
//				break;
			}
			if (!stockList.isEmpty()) {
				logger.info("stockList 0 : {}", stockList.get(0));
				// insert
				KrxDataCollections entity = KrxDataCollections.builder().mktId(reqVO.getData()).stockData(stockList)
						.build();
				
				// Repository 버전
				krxDataRepository.save(entity);
			}
			ls.setKrxList(stockList);
			long end = System.currentTimeMillis();
			
			NumberFormat formatter = new DecimalFormat("#0.00000");
			
			logger.info(">>>>>>>>>> finish Execution time is {}, {}", formatter.format((end - start) / 1000d),
					" seconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	private String encodingKey = "lwkSVwPXQW5eu%2FvZtxuxnGg8Mipyjp0QyeaOnfRmVsPrkyaNQWIE7r%2BE0ct%2BE8heWXJD3O2dhRLuiIaO%2F8EbHQ%3D%3D";
//	private String decodingKey = "lwkSVwPXQW5eu/vZtxuxnGg8Mipyjp0QyeaOnfRmVsPrkyaNQWIE7r+E0ct+E8heWXJD3O2dhRLuiIaO/8EbHQ==";

	public String getDataKrxAPI(KrxReqVO vo) throws Exception {
		StringBuilder urlBuilder = new StringBuilder(
				"http://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo"); /* URL */
		urlBuilder.append("?serviceKey=" + vo.getServiceKey()); /* Service Key */
		urlBuilder.append("&resultType=" + URLEncoder.encode(vo.getResultType(), "UTF-8"));
		urlBuilder.append("&basDt=" + URLEncoder.encode(vo.getBasDt(), "UTF-8"));
		urlBuilder.append("&likeSrtnCd=" + URLEncoder.encode(vo.getLikeSrtnCd(), "UTF-8"));
		logger.info("url: {}", urlBuilder.toString());

		StringBuilder sb = new StringBuilder();

		// SSL

//		HostnameVerifier hv = new HostnameVerifier() {
//            public boolean verify(String urlHostName, SSLSession session) {
//               System.out.println("Warning: URL Host: "+urlHostName+" vs. "
//                 +session.getPeerHost());
//               return true;
//            }
//        };
//
//        // set this property to the location of the cert file
////        System.setProperty("javax.net.ssl.trustStore", "jssecacerts.cert");
//
//        HttpsURLConnection.setDefaultHostnameVerifier(hv);
//        URL url = new URL(urlBuilder.toString());
//        HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
//
//        System.out.println("sending request...");
//        urlConn.setRequestMethod("GET");
////        urlConn.setAllowUserInteraction(false); // no user interaction
//        urlConn.setDoInput(true);
//
//        urlConn.setDoOutput(true); // want to send
////        urlConn.setRequestProperty( "Content-type", "application/json" );
//        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0");
////        urlConn.setRequestProperty( "accept", "application/json" );
////        urlConn.setRequestProperty( "authorization", "Basic " + 
////        		URLEncoder.encode("administrator:collation", "UTF-8"));
////        Map headerFields = urlConn.getHeaderFields();
////        System.out.println("header fields are: " + headerFields);
//
//        int responseCode = urlConn.getResponseCode();
//     // SSL setting
//        String line = null;
//		InputStream in = null;
//		BufferedReader reader = null; 
//
//	
//     			SSLContext context = SSLContext.getInstance("TLS");
//     			context.init(null, null, null); // No validation for now
//     			urlConn.setSSLSocketFactory(context.getSocketFactory());
//     			
//     			// Connect to host
//     			urlConn.connect();
//     			urlConn.setInstanceFollowRedirects(true);
//     			
//     			// Print response from host
//     			if (responseCode == HttpsURLConnection.HTTP_OK) { // 정상 호출 200
//     				in = urlConn.getInputStream();
//     			} else { // 에러 발생
//     				in = urlConn.getErrorStream();
//     			}
//     			reader = new BufferedReader(new InputStreamReader(in));
//     			while ((line = reader.readLine()) != null) {
//     				System.out.printf("%s\n", line);
//     				sb.append(line);
//     			}
//     			
//     			reader.close();

		// Normal

//		URL url = new URL(urlBuilder.toString());
//		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//		
//        SSLContext context = SSLContext.getInstance("TLS");
//        context.init(null, null, null); 
//        conn.setSSLSocketFactory(context.getSocketFactory());
//        conn.connect();
//        conn.setInstanceFollowRedirects(true);
//
//        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
//        BufferedReader reader = new BufferedReader(new InputStreamReader(bis));
//        String line = null;
//        while ((line = reader.readLine()) != null) {
//            System.out.printf("%s\n", line);
//            sb.append(line);
//        }
//		reader.close();

		URL url = new URL(urlBuilder.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-type", "application/json");
		logger.info("Response code: {}", conn.getResponseCode());
		BufferedReader rd;
		if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} else {
			rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		}
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
		conn.disconnect();
//		logger.info("sb: {}", sb.toString());

		return sb.toString();
	}

//	public List<Map<String, Object>> getDataAPI() throws Exception {
//		StringBuilder urlBuilder = new StringBuilder(
//				"http://api.seibro.or.kr/openapi/service/StockSvc/getKDRSecnInfo"); /* URL */
//		urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + encodingKey); /* Service Key */
//		urlBuilder.append("&" + URLEncoder.encode("caltotMartTpcd", "UTF-8") + "="
//				+ URLEncoder.encode("11", "UTF-8")); /* 11: 유가증권시장, 12: 코스닥시장, 13: 코넥스시장 */
//		URL url = new URL(urlBuilder.toString());
//		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		conn.setRequestMethod("GET");
//		conn.setRequestProperty("Content-type", "application/json");
//		System.out.println("Response code: " + conn.getResponseCode());
//		BufferedReader rd;
//		if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
//			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//		} else {
//			rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//		}
//		StringBuilder sb = new StringBuilder();
//		String line;
//		while ((line = rd.readLine()) != null) {
//			sb.append(line);
//		}
//		rd.close();
//		conn.disconnect();
//		logger.info("sb: {}", sb.toString());
//
//		return null;
//	}

//	public String extractPriceText(String text, String patternStr) throws Exception {
//		logger.info("text : {}", text);
//		List<String> array = new ArrayList<String>();
//		String price = null;
//
//		Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
//		Matcher matcher = pattern.matcher(text);
//		while (matcher.find()) { // 정규식과 매칭되는 값이 있으면
//			logger.info("all : {}", matcher.group());
////			if (matcher.group(1).trim().contains("price")) {
////				array.add(matcher.group(1).trim()); // 특정 단어 사이의 값을 추출한다
////			}
//		}
//
//		if (!array.isEmpty()) {
////			logger.info("string: {}", array.stream().reduce((first,second) -> second).orElse(null));			
//
//			// 테이블 Financial Highlight
////			text = array.stream().skip(3).findFirst().get();
//		}
////		logger.info("price : {}", price);
//
//		return price;
//	}

	public AllocationPrice extractTableText(String text) throws Exception {
//		String text = "배당수익률</span></a></div></th><td class=r   >&nbsp;</td>  <td class=r   >1.08</td>  <td class=r   >&nbsp;</td>  <td class=r   >1.72</td>  <td class=r   >&nbsp;</td>  <td class=r  ";
		List<String> array = new ArrayList<String>();
		List<String> rtnArray = new ArrayList<String>();
		AllocationPrice ap = new AllocationPrice();

//		Pattern pattern = Pattern.compile("[<table](.*?)[table>]", Pattern.DOTALL);
		Pattern pattern = Pattern.compile("<table.*?>(.*?)</table>", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) { // 정규식과 매칭되는 값이 있으면
			if (matcher.group(1).trim().contains("배당수익률")) {
				array.add(matcher.group(1).trim()); // 특정 단어 사이의 값을 추출한다
			}
		}
//		long count = array.stream().count();

		if (!array.isEmpty()) {
//			logger.info("string: {}", array.stream().reduce((first,second) -> second).orElse(null));			

			// 테이블 Financial Highlight
			text = array.stream().skip(3).findFirst().get();
//			logger.info("string: {}", text);

			pattern = Pattern.compile("<tr.*?>(.*?)</tr>", Pattern.DOTALL);
			matcher = pattern.matcher(text);
			array.clear();
			while (matcher.find()) { // 정규식과 매칭되는 값이 있으면
				array.add(matcher.group(1).trim()); // 특정 단어 사이의 값을 추출한다
			}
			long count = array.stream().count();
			text = array.stream().skip(count - 1).findFirst().get();
//			logger.info("tr : {}", text);

			pattern = Pattern.compile("<td.*?>(.*?)</td>", Pattern.DOTALL);
			matcher = pattern.matcher(text);
			array.clear();
			while (matcher.find()) { // 정규식과 매칭되는 값이 있으면
				array.add(matcher.group(1).trim()); // 특정 단어 사이의 값을 추출한다
			}
			int integerCnt = 0;
			double sum = 0.0;
			if (array == null || array.isEmpty()) {
				return ap;
			}
			for (int k = 0; k < 5; k++) {
				try {
					String string = array.get(k);
//				logger.info("string : {}", string);
					sum += Double.parseDouble(string);
					rtnArray.add(string);
					integerCnt++;
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				}
			}
			if (integerCnt > 0) {
				ap.setPriceList(rtnArray);
				ap.setAvgPrice(String.format("%.2f", sum / integerCnt));
			}
		}
//		logger.info("ap : {}", ap);

		return ap;
	}

//	public String searchHtmlText(String formatStr, String stockCode) throws Exception {
//		String url = String.format(formatStr, stockCode);
//
//		URL obj = new URL(url);
////		logger.debug("url : {}", url);
//		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
//
//		/************************ 인증서 적용 후 제거 할 것 START **********************/
//		con.setHostnameVerifier(new HostnameVerifier() {
//			@Override
//			public boolean verify(String hostname, SSLSession session) {
//				return true;
//			}
//		});
//		/************************ 인증서 적용 후 제거 할 것 END **********************/
//
//		// add reuqest header
//		con.setRequestMethod("GET"); // 전송 방식
////		con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
//		con.setConnectTimeout(5000); // 연결 타임아웃 설정(5초)
//		con.setReadTimeout(5000); // 읽기 타임아웃 설정(5초)
//
//		con.setDoOutput(true);
//
//		int responseCode = con.getResponseCode();
//
//		if (responseCode != 200) {
//			logger.error("연결 실패");
//			throw new Exception("NAVER API 연결 실패");
//		}
//		Charset charset = Charset.forName("UTF-8");
//
//		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
//		String inputLine;
//		StringBuilder response = new StringBuilder();
//
//		while ((inputLine = in.readLine()) != null) {
//			response.append(inputLine);
//		}
//		in.close();
//
//		return response.toString();
//	}

	public String searchFnGuideSiteHtmlText(String stockCode) throws Exception {
//		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A004450";
//		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A277810";
//		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A326030";
//		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A009900";
		String url = String.format("https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A%s", stockCode);

		URL obj = new URL(url);
//		logger.debug("url : {}", url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		/************************ 인증서 적용 후 제거 할 것 START **********************/
		con.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		/************************ 인증서 적용 후 제거 할 것 END **********************/

		// add reuqest header
		con.setRequestMethod("GET"); // 전송 방식
//		con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		con.setConnectTimeout(5000); // 연결 타임아웃 설정(5초)
		con.setReadTimeout(5000); // 읽기 타임아웃 설정(5초)

//		String json = g.toJson(jsonVal);
//
//		System.out.println(json);

		con.setDoOutput(true);
//		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
//		out.write(json.toString());
//		out.close();

		int responseCode = con.getResponseCode();
//		logger.info("Sending 'POST' request to URL : {}", url);
//		System.out.println("Post parameters : " + json);
//		logger.info("Response Code : {}", responseCode);

		if (responseCode != 200) {
			logger.error("연결 실패");
			throw new Exception("NAVER API 연결 실패");
		}
//		Charset charset = Charset.forName("EUC-KR");
		Charset charset = Charset.forName("UTF-8");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// response 값은 "{code:200, Agent:{ID:12398723418974}}" 형식
		return response.toString();
	}

	public String getDataPost(String type, String mkt, String yyyyMMdd) throws Exception {
		URL url = new URL("http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd");
		HttpURLConnection conn = null;
//		JSONObject responseJson = null;
//		String responseStr = null;

		conn = (HttpURLConnection) url.openConnection();

		// type의 경우 POST, GET, PUT, DELETE 가능
		conn.setRequestMethod(type);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        conn.setRequestProperty("Transfer-Encoding", "chunked");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setDoOutput(true);

		String bld = URLEncoder.encode("dbms/MDC/STAT/standard/MDCSTAT01501", "UTF-8");
		String mktId = URLEncoder.encode(mkt, "UTF-8");// ALL STK 코스피 KSQ 코스닥
		String trdDd = URLEncoder.encode(yyyyMMdd, "UTF-8");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("bld", bld);
		params.put("mktId", mktId);
		params.put("trdDd", trdDd);

		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> m : params.entrySet()) {
			if (postData.length() != 0) {
				postData.append("&");
			}
			postData.append(m.getKey());
			postData.append("=");
			postData.append(m.getValue());
		}
		byte[] postDataByte = postData.toString().getBytes();

		conn.setRequestProperty("Content-Length", String.valueOf(postDataByte.length));

		conn.getOutputStream().write(postDataByte);

		// 보내고 결과값 받기
		int responseCode = conn.getResponseCode();
		StringBuilder sb = new StringBuilder();
		if (responseCode == 200) {
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
//			logger.info("sb :: {}", sb.toString());

			// 응답 데이터
		}
		logger.info("getDataPost responseCode : {}, length : {}", responseCode, sb.length());
		// response 값은 "{code:200, Agent:{ID:12398723418974}}" 형식
		return sb.toString();
	}

	public List<String> extractAllocationPrice(String text) throws Exception {
//		String text = "배당수익률</span></a></div></th><td class=r   >&nbsp;</td>  <td class=r   >1.08</td>  <td class=r   >&nbsp;</td>  <td class=r   >1.72</td>  <td class=r   >&nbsp;</td>  <td class=r  ";
		List<String> array = new ArrayList<String>();

		Pattern pattern = Pattern.compile("[>](.*?)[</]");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) { // 정규식과 매칭되는 값이 있으면
			array.add(matcher.group(1).trim()); // 특정 단어 사이의 값을 추출한다
		}

		return array;
	}

	public String searchFnguideSite() throws Exception {
		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A004450";

		Document document = Jsoup.connect(url).get();
		logger.debug("document : {}", document);
		// 3. Parse the HTML to extract links to other URLs
		Elements linksOnPage = document.select("div#highlight_D_A table tbody tr");
		logger.debug("linksOnPage : {}", linksOnPage);

		// 5. For each extracted URL... go back to Step 4.
		for (Element page : linksOnPage) {
			logger.debug("html : {}", page.html());
		}

		return null;
	}
}