package com.etf.rest.svc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.json.simple.JSONObject;
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

import com.etf.rest.entity.StockInfoCollections;
import com.etf.rest.repo.EtfRepository;
import com.etf.rest.repo.StockRepository;
import com.etf.rest.util.Utils;
import com.etf.rest.vo.AllocationPrice;
import com.etf.rest.vo.Item;
import com.etf.rest.vo.ReqVO;
import com.etf.rest.vo.ResultVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StockInfoService {
	Logger logger = LoggerFactory.getLogger(StockInfoService.class);

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private EtfRepository etfRepository;
	
	@Autowired
	private StockRepository stockRepository;

	public ReqVO getData() {
		logger.debug("test : {}, {}", "한글이~~~", "abc");
		ReqVO vo = new ReqVO();
		vo.setResult("success");
		vo.setData("한글이깨지나?");
		return vo;
	}
	
	public ResultVO searchStock(ReqVO reqVO) {
		ResultVO ls = new ResultVO();
		try {
//			reqVO.getData();//STK 코스피 KSQ 코스닥
			String stockInfoString = searchMongoStockInfo(reqVO.getData());

			if(stockInfoString == null) {
				stockInfoString = getDataPost("POST", reqVO.getData(), Utils.toDate("yyyyMMdd"));

				//insert
				StockInfoCollections entity = StockInfoCollections.builder()
			        .mktId(reqVO.getData())
			        .info(stockInfoString)
			        .build();

			    //Repository 버전
				stockRepository.save(entity);
			}
			
			logger.info("stockInfoString : {}", stockInfoString);
			
			List<Item> dataList = stockList(stockInfoString);
			ls.setStockList(dataList);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	public String searchMongoStockInfo(String key) throws Exception {

		StockInfoCollections stock = mongoOperations.findOne(Query.query(Criteria.where("mktId").is(key)), StockInfoCollections.class);
		logger.debug("stock : {}", stock);
		if(stock != null) {
			return stock.getInfo();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Item> stockList(String stockStr) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(stockStr);
		Map<String, Object> map = mapper.readValue(stockStr, Map.class);

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
		//흥아해운 003280
		//{ISU_SRT_CD=003280, ISU_CD=KR7003280005, ISU_ABBRV=흥아해운, MKT_NM=KOSPI, SECT_TP_NM=, TDD_CLSPRC=1,835, FLUC_TP_CD=1, CMPPREVDD_PRC=20, FLUC_RT=1.10, TDD_OPNPRC=1,815, TDD_HGPRC=1,845, TDD_LWPRC=1,790, ACC_TRDVOL=343,200, ACC_TRDVAL=621,072,880, MKTCAP=441,179,689,665, LIST_SHRS=240,424,899, MKT_ID=STK}
		logger.info("actualObj : {}", actualObj.size());
		return items;
	}

	public ResultVO search() {
		ResultVO ls = new ResultVO();
		try {
//			String text = searchFnGuideSiteHtmlText();

//			int startIndex = text.lastIndexOf("배당수익률");
////			logger.info("startIndex : {}, text: {}", startIndex, text);
//
//			text = text.substring(startIndex);
//			text = text.substring(0, text.indexOf("tdbg_b"));
//			logger.info("startIndex : {}, text: {}", startIndex, text);
//			
//			extractAllocationPrice(text);
//			extractTableText(text);

//			getDataAPI();
			getDataPost("POST", "KSQ", Utils.toDate("yyyyMMdd"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	private String encodingKey = "lwkSVwPXQW5eu%2FvZtxuxnGg8Mipyjp0QyeaOnfRmVsPrkyaNQWIE7r%2BE0ct%2BE8heWXJD3O2dhRLuiIaO%2F8EbHQ%3D%3D";
//	private String decodingKey = "lwkSVwPXQW5eu/vZtxuxnGg8Mipyjp0QyeaOnfRmVsPrkyaNQWIE7r+E0ct+E8heWXJD3O2dhRLuiIaO/8EbHQ==";

	public List<Map<String, Object>> getDataAPI() throws Exception {
		StringBuilder urlBuilder = new StringBuilder(
				"http://api.seibro.or.kr/openapi/service/StockSvc/getKDRSecnInfo"); /* URL */
		urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + encodingKey); /* Service Key */
		urlBuilder.append("&" + URLEncoder.encode("caltotMartTpcd", "UTF-8") + "="
				+ URLEncoder.encode("11", "UTF-8")); /* 11: 유가증권시장, 12: 코스닥시장, 13: 코넥스시장 */
		URL url = new URL(urlBuilder.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-type", "application/json");
		System.out.println("Response code: " + conn.getResponseCode());
		BufferedReader rd;
		if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} else {
			rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		}
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
		conn.disconnect();
		logger.info("sb: {}", sb.toString());

		return null;
	}

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
			for (int k = 0; k < 5; k++) {
				String string = array.get(k);
//				logger.info("string : {}", string);
				try {
					sum += Double.parseDouble(string);
					rtnArray.add(string);
					integerCnt++;
				} catch (NumberFormatException e) {
				}
			}
			if (integerCnt > 0) {
				ap.setPriceList(rtnArray);
				ap.setAvgPrice(String.format("%.2f", sum / integerCnt));
			}
		}
		logger.info("ap : {}", ap);

		return ap;
	}

	public String searchFnGuideSiteHtmlText() throws Exception {
//		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A004450";
//		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A277810";
//		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A326030";
//		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A009900";
		String url = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?gicode=A302430";

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
		JSONObject responseJson = null;
		String responseStr = null;

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
		if (responseCode == 200) {
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			logger.info("sb :: {}", sb.toString());

			// 응답 데이터
		}

		// response 값은 "{code:200, Agent:{ID:12398723418974}}" 형식
		return responseStr;
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