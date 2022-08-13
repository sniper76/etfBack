package com.etf.rest.svc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.etf.rest.entity.EtfInfoCollections;
import com.etf.rest.repo.EtfRepository;
import com.etf.rest.vo.EtfItem;
import com.etf.rest.vo.EtfItemList;
import com.etf.rest.vo.EtfResponse;
import com.etf.rest.vo.GridItem;
import com.etf.rest.vo.Item;
import com.etf.rest.vo.ReqVO;
import com.etf.rest.vo.ResultVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class DartService {
	Logger logger = LoggerFactory.getLogger(DartService.class);

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private EtfRepository etfRepository;

	public ReqVO getData() {
		logger.debug("test : {}, {}", "한글이~~~", "abc");
		ReqVO vo = new ReqVO();
		vo.setResult("success");
		vo.setData("한글이깨지나?");
		return vo;
	}

	public ResultVO search(ReqVO param) {
		logger.debug("test : {}", param);
		ResultVO ls = new ResultVO();
		try {
			//etfTabCode
			if(param.getResult() == null) {
				param.setResult("2");
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String toDay = sdf.format(new Date());

			String etfInfoString = searchMongoEtfInfo(toDay, param.getData());

			if(etfInfoString == null) {
				etfInfoString = searchNaverFinanceApi();

				//insert
				EtfInfoCollections entity = EtfInfoCollections.builder()
			        .date(toDay)
			        .info(etfInfoString)
			        .build();

			    //Repository 버전
				etfRepository.save(entity);
			}

			List<EtfItem> etfList = selectEtfItemList(etfInfoString, param);
			List<Item> stockList = removeDuplicatesItem(extractStockItem(selectEtfItemList(etfInfoString, param)));

			ls.setEtfList(etfList);
			ls.setStockList(stockList);
//			logger.info("ls : {}", ls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	public String searchMongoEtfInfo(String key, String searchText) throws Exception {

		EtfInfoCollections etf = mongoOperations.findOne(Query.query(Criteria.where("date").is(key)), EtfInfoCollections.class);
		logger.debug("etf : {}", etf);
		if(etf != null) {
			return etf.getInfo();
		}
		return null;
	}

	public String searchNaverFinanceApi() throws Exception {
		String url = "https://finance.naver.com/api/sise/etfItemList.nhn";

		URL obj = new URL(url);
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
		logger.info("Response Code : {}", responseCode);

		if (responseCode != 200) {
			logger.error("연결 실패");
			throw new Exception("NAVER API 연결 실패");
		}
		Charset charset = Charset.forName("EUC-KR");

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

	private List<EtfItem> selectEtfItemList(String responseText, ReqVO vo) throws Exception {

		Gson g = new Gson();
		EtfResponse etfRes = g.fromJson(responseText, EtfResponse.class);

//		logger.info("etfRes : {}", etfRes);

		List<EtfItem> resultList = new ArrayList<>();
		if(etfRes != null) {
			EtfItemList lst = etfRes.getResult();
			for (EtfItem item : lst.getEtfItemList()) {
				if(item.getItemname().contains(vo.getData()) && item.getEtfTabCode() == Integer.parseInt(vo.getResult())) {
					resultList.add(item);
				}
			}
		}
		return resultList;
	}

	public List<Item> extractStockItem(List<EtfItem> resultList) throws Exception {

		List<Item> extractData = new ArrayList<>();

//		logger.debug("etfList : {}", resultList);

		String itemUrl = "http://comp.wisereport.co.kr/ETF/ETF.aspx?cn=&cmp_cd=";
		for (EtfItem etfItem : resultList) {
//			if(etfItem.getEtfTabCode() != 2) {
//				continue;
//			}
	//				if("305720".equals(etfItem.getItemcode())) {
	//					if("305540".equals(etfItem.getItemcode())) {

			Document doc = Jsoup.connect(String.format("%s%s", itemUrl, etfItem.getItemcode())).get();

			String cuData = doc.outerHtml();

			cuData = getStringCuData(cuData);
//			logger.info("cuData : {}", cuData);

	//					Map<String, Object> data = makeJson(cuData);

	//					if(data != null) {
	//						String grid_data = data.get("grid_data").toString();

			GridItem gi = makeGson(cuData);
//			logger.info("gi : {}", gi.getGrid_data());

			setItemsMerge(extractData, gi.getGrid_data());

	//						for( Map.Entry<String, Object> elem : data.entrySet() ){
	//							System.out.println( String.format("키 : %s, 값 : %s", elem.getKey(), elem.getValue()) );
	//						}
	//					}
	//				}
		}
		return extractData;
	}

	private List<Item> removeDuplicatesItem(List<Item> items) throws Exception {
		List<Item> returnItems = new ArrayList<>();

		for (Item r : items) {
			Item firstElement = returnItems.stream()
			        .filter(s -> r.getSTK_NM_KOR().equals(s.getSTK_NM_KOR())).findFirst().orElse(null);
			if(firstElement != null) {
				Optional<Item> element = returnItems.stream()
					.filter(s -> r.getSTK_NM_KOR().equals(s.getSTK_NM_KOR())).findFirst();

				element.get().setSTK_NM_CNT(element.get().getSTK_NM_CNT()+1);
			}
			else {
				r.setSTK_NM_CNT(1);
				returnItems.add(r);
			}
		}

		Collections.sort(returnItems);

		return returnItems;
	}

	private void setItemsMerge(List<Item> master, List<Item> data) {
		master.addAll(data);
	}

	private GridItem makeGson(String j) {
	//	System.out.println(j);
		Gson gson = new GsonBuilder().create();
		GridItem r = gson.fromJson(j, GridItem.class);
		return r;
	}

	private String getStringCuData(String txt) {
		Scanner scanner = new Scanner(txt);
		String rtn = null;
		while (scanner.hasNextLine()) {
		  String line = scanner.nextLine();
		  if(line != null && line.length() > 0 ) {
			  if(line.contains("var CU_data")) {
				  rtn = line.replace("var CU_data = ", "");
				  rtn = rtn.substring(0, rtn.length()-1);
				  break;
			  }
		  }
		  // process the line
		}
		scanner.close();
		return rtn;
	}
}