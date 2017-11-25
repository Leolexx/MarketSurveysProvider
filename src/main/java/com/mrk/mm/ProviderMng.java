package com.mrk.mm;

import java.util.List;

import com.mrk.domain.MarketSurvey;
import com.mrk.domain.RequestMessage;

public interface ProviderMng {
	
	public void loadSurvey();
	public void addConsumerSubscription(RequestMessage msg);
	public List<MarketSurvey> getSurvey(RequestMessage msg, boolean isFromConsumer);
}
