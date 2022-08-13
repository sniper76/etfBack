package com.etf.rest.svc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import com.etf.rest.repo.EtfRepository;
import com.etf.rest.vo.AllocationPrice;
import com.etf.rest.vo.ReqVO;
import com.etf.rest.vo.ResultVO;

@Service
public class StockService {
	Logger logger = LoggerFactory.getLogger(StockService.class);

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

	public ResultVO search() {
		ResultVO ls = new ResultVO();
		try {
			String text = searchFnGuideSiteHtmlText();

//			int startIndex = text.lastIndexOf("배당수익률");
////			logger.info("startIndex : {}, text: {}", startIndex, text);
//
//			text = text.substring(startIndex);
//			text = text.substring(0, text.indexOf("tdbg_b"));
//			logger.info("startIndex : {}, text: {}", startIndex, text);
//			
//			extractAllocationPrice(text);
			extractTableText(text);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
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