package com.mrk.mm.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.mrk.domain.MarketSurvey;
import com.mrk.domain.RequestMessage;
import com.mrk.domain.RequestMessage.Survey;
import com.mrk.domain.RequestMessage.Survey.Target;
import com.mrk.mm.ProviderMng;
import com.mrk.repositories.MarketSurveyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProviderMngImpl implements ProviderMng {

	@Autowired
	MarketSurveyRepository mrep;

	@Autowired
	MongoTemplate mngt;

	// Store for Consumer's Messages for subscription
	private List<RequestMessage> messStore = new ArrayList<RequestMessage>();	
	
	/**
	 * Initial load of surveys
	 */
	@Override
	public void loadSurvey() {
		addSurvey(1, "81111601", "M", "ES", 31, "EUR", 20000);
		addSurvey(2, "81111608", "M", "ES", 53, "EUR", 60000);
		addSurvey(3, "81111608", "M", "RU", 23, "RUB", 24000);
		addSurvey(4, "81111608", "M", "RU", 46, "RUB", 56000);
		addSurvey(5, "81111608", "F", "ES", 21, "EUR", 25000);
		addSurvey(6, "81111601", "M", "RU", 34, "RUB", 30000);
		addSurvey(7, "81111601", "F", "RU", 26, "RUB", 35000);
		addSurvey(8, "81111601", "F", "ES", 45, "EUR", 45000);
		addSurvey(9, "81111601", "F", "US", 55, "USD", 55000);
		addSurvey(10, "81111607", "M", "US", 47, "USD", 50000);
		addSurvey(11, "81111608", "F", "US", 61, "USD", 51000);
		addSurvey(12, "81111608", "F", "US", 21, "USD", 34000);
		addSurvey(13, "81111608", "F", "ES", 34, "EUR", 55000);
		addSurvey(14, "81111609", "M", "ES", 28, "EUR", 19000);
		addSurvey(15, "81111608", "F", "RU", 28, "RUB", 39000);
		log.info("Surveys loaded...");
	}
	
	/**
	 * Add survey
	 * @param subject - Subject
	 * @param gender - Gender
	 * @param age - Age
	 * @param currency - Currency
	 * @param income - income
	 */
	private void addSurvey(Integer id, String subject, String gender, String country, Integer age, String currency, Integer income) {
		MarketSurvey survey = new MarketSurvey(id, subject, gender, country, age, currency, income);
		mrep.save(survey);
	}

	public List<RequestMessage> getMessStore() {
		return messStore;
	}

	/**
	 * Add new subscription
	 * @param msg - Request Message
	 */
	@Override
	public synchronized void addConsumerSubscription(RequestMessage msg) {
		if (msg.getSubscription().getIsClearPrev()) {
			// delete existing Consumer's subscriptions
			log.info("Consumer id={}, all subscriptions deleted!", msg.getRequester().getId());
			delSubscription(msg);
		}
		// add subscription
		messStore.add(msg);
	}

	/*
	 * Send Surveys by subscriptions every 5 sec
	 */
	@Scheduled(fixedRate = 5000)
    public void timer() {
    	sendSurveyBySubsrciption();
    }
    
	
	/**
	 * Subscription: Send Surveys to Consumer
	 */
 	private void sendSurveyBySubsrciption() {
 		RestTemplate restTemplate = new RestTemplate();
 		// find all subscriptions
 		Iterator<RequestMessage> i = messStore.iterator();
 		while (i.hasNext()) {
 			RequestMessage t = i.next();
 			if (t.getSubscription()!=null && t.getSubscription().getChannel().contains("rest")) {
 				// Way of delivery - RESTful
	 	 		List<MarketSurvey> lst = getSurvey(t, false);
		 		String url = "http://localhost:8089/postSurvey"; // TODO
		 		log.info("");
		 		log.info("Market Survey Provider: SEND SUBSCRIPTION: Cosumer.id={}, size={}, Url={}", t.getRequester().getId(), lst.size(), url);
		 		
		 		HttpEntity<List<MarketSurvey>> requestEntity = new HttpEntity<List<MarketSurvey>>(lst);
		 		try {
					restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
				} catch (RestClientException e) {
					log.error("Can't connect to Consumer with Id="+t.getRequester().getId()+" Url="+url);
					// Delete subscriptions with lost contact with Consumer
					i.remove();
				}
			} // TODO: Another ways of delivery
	 		
 		}
 		
	}

	private void delSubscription(RequestMessage msg) {
		Iterator<RequestMessage> i = messStore.iterator();
		while (i.hasNext()) {
		   RequestMessage mes = i.next();
		   if (mes.getRequester().getId().equals(msg.getRequester().getId())) {
			   i.remove();
		   }
		}
	}
	
 	
 	/**
 	 * Get Surveys from NOSQL database
 	 * @param msg - Request Message
 	 * @param isFromConsumer - Type of processing
 	 * @return
 	 */
 	@Override
 	public List<MarketSurvey> getSurvey(RequestMessage msg, boolean isFromConsumer) {
 		Survey survey = msg.getSurvey();
 		Target target = msg.getSurvey().getTarget();

 		if (isFromConsumer) {
			log.info("");
	 		log.info("Market Survey Provider: GOT /getSurveyByReq:");
	 		log.info("Requester.id={}, Requester.name={}", msg.getRequester().getId(), msg.getRequester().getName());
	 		log.info("Provider.id={}, Provider.name={}", msg.getProvider().getId(), msg.getProvider().getName());

	 		log.info("Find Surveys with parameters:");
	 		log.info("Subject={}", msg.getSurvey().getSubject());
	 		log.info("Survey.country={}", survey.getCountry());
	 		log.info("Survey.target.gender={}", target.getGender());
	 		log.info("Survey.target.income.currency={}", target.getIncome().getCurrency());
	 		target.getAge().forEach(t-> {
	 	 		log.info("Survey.target.age={}", t);
	 		});
	 		target.getIncome().getRange().forEach(t-> {
	 	 		log.info("Survey.target.income.range={}", t);
	 		});
	 		if (msg.getSubscription()!= null) {
	 			// Save message for subscription
	 			addConsumerSubscription(msg);
	 		}
 		}
 		
 		List<Integer> age = target.getAge();
 		List<Integer> income = target.getIncome().getRange();
 		
 		// filtering surveys
 		// by subject, ageFrom, ageTo, incomeFrom, incomeTo, gender, currency, country
		List<MarketSurvey> lst = mrep.findCustom(
 				survey.getSubject(), 
 				age.get(0), age.get(1),
 				income.get(0), income.get(1),
 				target.getGender(),
 				target.getIncome().getCurrency(),
 				survey.getCountry()
 				);
 		return lst;
 	}
 	
 	
}
