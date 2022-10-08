package com.etf.rest.ctrl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.etf.rest.entity.DatabaseSequence;
import com.etf.rest.entity.LogCollections;
import com.etf.rest.repo.LogRepository;
import com.etf.rest.svc.DartService;
import com.etf.rest.svc.StockInfoService;
import com.etf.rest.svc.StockService;
import com.etf.rest.vo.ReqVO;
import com.etf.rest.vo.ResultVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증", description = "인증 관련 api 입니다.")
@RestController
@RequestMapping("/api")
public class SampleController {

	@Autowired
	private DartService dartService;
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private StockInfoService stockInfoService;

//	@Autowired
//	private MongoTemplate mongoTemplate;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private MongoOperations mongoOperations;

	private Logger logger = LoggerFactory.getLogger(SampleController.class);
	
	
	@Operation(summary = "로그인 메서드", description = "로그인 메서드입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = ReqVO.class))),
        @ApiResponse(responseCode = "400", description = "bad request operation", content = @Content(schema = @Schema(implementation = ReqVO.class)))
    })
	@GetMapping("/hello")
	public ReqVO hello() {
		return dartService.getData();
	}

	@GetMapping("/fnguide")
	public ResultVO fnguide(@RequestParam(name="id") String mktId) {
//		accessLog(request, model);
		ReqVO vo = new ReqVO();
		vo.setData(mktId);
		return stockInfoService.fnguide(vo);
	}
	
	@GetMapping("/stockData") 
	public ResultVO stockData(@RequestParam(name="id") String mktId) {
//		accessLog(request, model);
		ReqVO vo = new ReqVO();
		vo.setData(mktId);
		return stockInfoService.stockData(vo);
	}
	
	@Operation(summary = "KRX 정보 등록", description = "KRX 코스피STK 코스닥KSQ 20200108,20200319,20220713일자 정보 등록")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = ReqVO.class))),
        @ApiResponse(responseCode = "400", description = "bad request operation", content = @Content(schema = @Schema(implementation = ReqVO.class)))
    })
	@GetMapping("/krxData") 
	public ResultVO searchKrx(@RequestParam(name="id") String mktId) {
//		accessLog(request, model);
		ReqVO vo = new ReqVO();
		vo.setData(mktId);
		return stockInfoService.searchKrx(vo);
	}

	@Operation(summary = "KRX 정보 등록", description = "KRX 코스피STK 코스닥KSQ 입력일자 정보 등록")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = ReqVO.class))),
        @ApiResponse(responseCode = "400", description = "bad request operation", content = @Content(schema = @Schema(implementation = ReqVO.class)))
    })
	@GetMapping("/dayData") 
	public ResultVO dayData(@RequestParam(name="id") String mktId, @RequestParam(name="dt") String date) {
//		accessLog(request, model);
		ReqVO vo = new ReqVO();
		vo.setData(mktId);
		vo.setSearchText(date);
		return stockInfoService.dayData(vo);
	}

	@PostMapping("/search")
	@ResponseBody
	public ResultVO search(HttpServletRequest request, @RequestBody ReqVO model) {
		accessLog(request, model);
		return dartService.search(model);
	}
	
	@PostMapping("/searchItem")
	@ResponseBody
	public ResultVO searchItem(HttpServletRequest request, @RequestBody ReqVO model) {
		accessLog(request, model);
		return dartService.searchItem(model);
	}
	
	@PostMapping("/stock")
	@ResponseBody
	public ResultVO stock(HttpServletRequest request, @RequestBody ReqVO model) {
		accessLog(request, model);
//		ReqVO vo = new ReqVO();
//		vo.setData(mktId);
		return stockInfoService.searchKrxData(model);
	}
	
	@PostMapping("/stock/toDay")
	@ResponseBody
	public ReqVO stockToDay(HttpServletRequest request, @RequestBody ReqVO model) {
		accessLog(request, model);
//		ReqVO vo = new ReqVO();
//		vo.setData(mktId);
		return stockInfoService.searchToDayPrice(model);
	}

	public void accessLog(HttpServletRequest request, @RequestBody ReqVO model) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		LogCollections entity = LogCollections.builder()
				.id(generateSequence(LogCollections.SEQUENCE_NAME))
	        .date(sdf.format(new Date()))
	        .keyword(model.getData())
	        .address(getClientIP(request))
	        .build();

	    //Repository 버전
		logRepository.save(entity);

//	    //mongoTemplate 버전
//		mongoTemplate.insert(entity);
	}

	public Long generateSequence(String seqName) {
	    DatabaseSequence counter = mongoOperations.findAndModify(Query.query(Criteria.where("_id").is(seqName)),
	    	      new Update().inc("seq",1),
	    	      FindAndModifyOptions.options().returnNew(true).upsert(true),
	    	      DatabaseSequence.class);
	    return !Objects.isNull(counter) ? counter.getSeq() : 1;
	}

	private String getClientIP(HttpServletRequest request) {
	    String ip = request.getHeader("X-Forwarded-For");
//	    logger.info("> X-FORWARDED-FOR : " + ip);

	    if (ip == null) {
	        ip = request.getHeader("Proxy-Client-IP");
//	        logger.info("> Proxy-Client-IP : " + ip);
	    }
	    if (ip == null) {
	        ip = request.getHeader("WL-Proxy-Client-IP");
//	        logger.info(">  WL-Proxy-Client-IP : " + ip);
	    }
	    if (ip == null) {
	        ip = request.getHeader("HTTP_CLIENT_IP");
//	        logger.info("> HTTP_CLIENT_IP : " + ip);
	    }
	    if (ip == null) {
	        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
//	        logger.info("> HTTP_X_FORWARDED_FOR : " + ip);
	    }
	    if (ip == null) {
	        ip = request.getRemoteAddr();
//	        logger.info("> getRemoteAddr : "+ip);
	    }
	    logger.info("> Result : IP Address : "+ip);

	    return ip;
	}
}
