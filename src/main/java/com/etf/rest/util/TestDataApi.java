package com.etf.rest.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TestDataApi {

	public static void main(String[] args) {
		try {
			// 1. URL을 만들기 위한 StringBuilder.
			StringBuilder urlBuilder = new StringBuilder(
					"http://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo"); /* URL */
			// 2. 오픈 API의요청 규격에 맞는 파라미터 생성, 발급받은 인증키.
			/* Service Key */
			urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8")
					+ "=lwkSVwPXQW5eu%2FvZtxuxnGg8Mipyjp0QyeaOnfRmVsPrkyaNQWIE7r%2BE0ct%2BE8heWXJD3O2dhRLuiIaO%2F8EbHQ%3D%3D");
			urlBuilder.append("&" + URLEncoder.encode("resultType", "UTF-8") + "="
					+ URLEncoder.encode("json", "UTF-8")); /* XML 또는 JSON */

			urlBuilder.append("&" + URLEncoder.encode("basDt", "UTF-8") + "=" + URLEncoder.encode("20220713", "UTF-8"));
			urlBuilder.append(
					"&" + URLEncoder.encode("likeSrtnCd", "UTF-8") + "=" + URLEncoder.encode("091700", "UTF-8"));
			// 3. URL 객체 생성.
			URL url = new URL(urlBuilder.toString());
			System.out.println(urlBuilder.toString());
			// 4. 요청하고자 하는 URL과 통신하기 위한 Connection 객체 생성.
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 5. 통신을 위한 메소드 SET.
			conn.setRequestMethod("GET");
			// 6. 통신을 위한 Content-type SET.
			conn.setRequestProperty("Content-type", "application/json");
			// 7. 통신 응답 코드 확인.
			System.out.println("Response code: " + conn.getResponseCode());
			// 8. 전달받은 데이터를 BufferedReader 객체로 저장.
			BufferedReader rd;
			if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} else {
				rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}
			// 9. 저장된 데이터를 라인별로 읽어 StringBuilder 객체로 저장.
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			// 10. 객체 해제.
			rd.close();
			conn.disconnect();
			// 11. 전달받은 데이터 확인.
			System.out.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
