package com.etf.rest.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.etf.rest.vo.EtfItem;
import com.etf.rest.vo.EtfItemList;
import com.etf.rest.vo.EtfResponse;
import com.etf.rest.vo.GridItem;
import com.etf.rest.vo.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpsRequestExample {	
	private final String USER_AGENT = "Mozilla/5.0";
	
	private List<Item> extractData = null;

	public void requestData(JSONObject jsonVal) throws Exception {
		String url = "https://finance.naver.com/api/sise/etfItemList.nhn";

		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		/************************ 인증서 적용 후 제거 할 것 START **********************/
		con.setHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		/************************ 인증서 적용 후 제거 할 것 END **********************/

		// add reuqest header
		con.setRequestMethod("GET");
//		con.setRequestProperty("User-Agent", USER_AGENT);
//		con.setRequestProperty("Content-Type", "application/json");
//		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		Gson g = new Gson();
//		String json = g.toJson(jsonVal);
//
//		System.out.println(json);

		con.setDoOutput(true);
//		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
//		out.write(json.toString());
//		out.close();

		int responseCode = con.getResponseCode();
		System.out.println("Sending 'POST' request to URL : " + url);
//		System.out.println("Post parameters : " + json);
		System.out.println("Response Code : " + responseCode);

		if (responseCode != 200) {
			System.out.println("연결 실패");
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// response 값은 "{code:200, Agent:{ID:12398723418974}}" 형식
		
		EtfResponse etfRes = g.fromJson(response.toString(), EtfResponse.class);
		
//		System.out.println(etfRes);
		
		String searchText = "2차전지";

		if(etfRes != null) {
			List<EtfItem> resultList = new ArrayList<EtfItem>();
			EtfItemList lst = etfRes.getResult();
			for (EtfItem item : lst.getEtfItemList()) {
				if(item.getItemname().contains(searchText)) {
					resultList.add(item);
				}
			}
			
			if(resultList.size() > 0) {
				extractData = new ArrayList<Item>();
				
				String itemUrl = "http://comp.wisereport.co.kr/ETF/ETF.aspx?cn=&cmp_cd=";
				for (EtfItem etfItem : resultList) {
					if(etfItem.getEtfTabCode() != 2) {
						continue;
					}
//					if("305720".equals(etfItem.getItemcode())) {
//						if("305540".equals(etfItem.getItemcode())) {
						System.out.println(etfItem.toString());
						
						Document doc = Jsoup.connect(String.format("%s%s", itemUrl, etfItem.getItemcode())).get();
	
						String cuData = doc.outerHtml();
						
						cuData = getStringCuData(cuData);
//						System.out.println(cuData);
						
//						Map<String, Object> data = makeJson(cuData);

//						if(data != null) {
//							String grid_data = data.get("grid_data").toString();
							
							GridItem gi = makeGson(cuData);
//							System.out.println(gi.getGrid_data());
							
							setItemsMerge(extractData, gi.getGrid_data());
							
//							for( Map.Entry<String, Object> elem : data.entrySet() ){
//								System.out.println( String.format("키 : %s, 값 : %s", elem.getKey(), elem.getValue()) );
//							}
//						}
//					}
							System.out.println("\n");
							Thread.sleep(1000);
				}
				
				removeDuplicatesItem(extractData);
			}
		}
	}
	
	private void removeDuplicatesItem(List<Item> items) {
		Map<String, Integer> map = new HashMap<>();

		for (Item r : items) {
			if("원화현금".equals(r.getSTK_NM_KOR())) {
				continue;
			}
			if (map.containsKey(r.getSTK_NM_KOR())) {
				map.put(r.getSTK_NM_KOR(), map.get(r.getSTK_NM_KOR()) + 1);
			} // if
			else {
				map.put(r.getSTK_NM_KOR(), 1);
			}
		} // for

		printItem(map);
	}
	
	private void printItem(Map<String, Integer> map) {
		// iterate
		Stream<Map.Entry<String, Integer>> sorted =
			    map.entrySet().stream()
			       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
		
		sorted.forEach(System.out::println);
	}
	
	private void setItemsMerge(List<Item> master, List<Item> data) {
		master.addAll(data);
	}
	
	private GridItem makeGson(String j) {
//		System.out.println(j);
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
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> makeJson(String txt) {
		ObjectMapper mapper = new ObjectMapper();

		Map<String, Object> map = null;
		try {
			map = mapper.readValue(txt, Map.class);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;
	}
}
