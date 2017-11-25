package com.mrk.mm.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mrk.domain.MarketSurvey;
import com.mrk.domain.RequestMessage;
import com.mrk.mm.ProviderMng;
import com.mrk.repositories.MarketSurveyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ProviderRestController {

	@Autowired
	MarketSurveyRepository mrep;

	@Autowired 
	ProviderMng providerMng;
	/**
	 * Get surveys by RequestMessage 
	 * @param msg - Request message
	 * @return - Survey data
	 */
	@RequestMapping(value = "/getSurveyByReq", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
 	public List<MarketSurvey> getSurveyByReq(@RequestBody RequestMessage msg) {
		log.info("GOT /getSurveyByReq");
		// get Survey, set subscription
 		return providerMng.getSurvey(msg, true);
 	}

	
	/**
	 * Get all available surveys
	 * @return - Distinct list of all survey's subjects  
	 */
	@RequestMapping(value = "/getSurvey", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
 	public List<String> getSurvey() {
		log.info("GOT /getSurvey");
		return mrep.findAll().stream().map(t-> t.getSubject()).distinct().collect(Collectors.toList());
	}
	
	
}
