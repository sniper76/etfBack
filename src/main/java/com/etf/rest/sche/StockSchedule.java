package com.etf.rest.sche;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.etf.rest.svc.StockInfoService;
import com.etf.rest.util.Utils;

@SpringBootApplication
@EnableScheduling
public class StockSchedule {
	Logger logger = LoggerFactory.getLogger(StockSchedule.class);
	
	@Autowired
	private StockInfoService stockInfoService;
	
	@Scheduled(cron = "0 8 22 * * 1-6") // 매월 15일 오전 10시 15분에 실행
	public void scheduleTaskUsingCronExpressionStk() {
		
		stockInfoService.searchDailyStock(Utils.toDate("yyyyMMdd"), "STK");
		
//		logger.debug("test : {}, {}", "한글이~~~", "abc");
//		logger.info("test : {}, {}", "한글이~~~", "abc");
	}
	
	@Scheduled(cron = "0 28 22 * * 1-6")
	public void scheduleTaskUsingCronExpressionKsq() {
		
		stockInfoService.searchDailyStock(Utils.toDate("yyyyMMdd"), "KSQ");
	}
}
