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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.etf.rest.entity.DatabaseSequence;
import com.etf.rest.entity.LogCollections;
import com.etf.rest.repo.LogRepository;
import com.etf.rest.svc.DartService;
import com.etf.rest.vo.ReqVO;
import com.etf.rest.vo.ResultVO;

@RestController
//@RequestMapping("/sample")
public class SampleController {
	
	@Autowired
	private DartService dartService;
	
//	@Autowired
//	private MongoTemplate mongoTemplate;

	@Autowired
	private LogRepository logRepository;
	
	@Autowired
	private MongoOperations mongoOperations;
	
	Logger logger = LoggerFactory.getLogger(SampleController.class);

	@GetMapping("/hello")
	public ReqVO hello() {
		return dartService.getData();
	}
	
	@PostMapping("/api/search")
	@ResponseBody
	public ResultVO search(HttpServletRequest request, @RequestBody ReqVO model) {
		accessLog(request, model);
		return dartService.search(model);
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
