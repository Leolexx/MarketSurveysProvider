package com.mrk.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mrk.domain.MarketSurvey;


public interface MarketSurveyRepository extends MongoRepository<MarketSurvey, Long> {

    @Query("{ 'age' : { $gte: ?0, $lte: ?1 } }") // TODO
    List<MarketSurvey> findCustomByName(Integer age1, Integer age2);

    @Query("{$and:[{'subject':?0}, {'age' : { $gte: ?1, $lte: ?2 } } , {'income' : { $gte: ?3, $lte: ?4 } } , {'gender':?5}, {'currency':?6}, {'country':?7}  ] }")
    List<MarketSurvey> findCustom(String subject, Integer ageFrom, Integer ageTo, 
    		Integer incomeFrom, Integer incomeTo, String gender, String currency,
    		String country);

}