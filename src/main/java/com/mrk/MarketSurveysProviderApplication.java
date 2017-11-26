package com.mrk;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mrk.mm.ProviderMng;
import com.mrk.mm.impl.ProviderMngImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class MarketSurveysProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketSurveysProviderApplication.class, args);
		log.info("MarketSurveysProviderApplication started...");
	}
	
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

        	// Initial loading of all surveys
        	ProviderMng mng=(ProviderMng)ctx.getBean(ProviderMngImpl.class);
        	mng.loadSurvey();
        };
    }

}
