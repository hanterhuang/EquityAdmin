package com.hcc.EquityAdmin.api;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.EquityAdmin.model.EquityTransaction;
import com.hcc.EquityAdmin.service.EquityEventService;
import com.hcc.EquityAdmin.util.TransferTrade;

@RestController
@RequestMapping(value = "/equity")
public class EquityController {
	
	@Value("${action.type}")
	private String actionType;
	
	@Value("${biz.type}")
	private String bizType;
	
	@Autowired
	private EquityEventService eventService;
	
    @GetMapping(value = "/trade/come/{transaction}")
    public ConcurrentHashMap<String, Integer> getPosition(@PathVariable("transaction") String transaction) throws Exception{
    	try {
    		EquityTransaction et = TransferTrade.toEquityTransction(transaction, actionType, bizType);
    		eventService.doTransaction(et);
        	
    	}catch(Exception e) {
    		e.printStackTrace();
    		// transaction format exception, just ignore the current coming transaction
    	}
    	
    	Thread.sleep(100);
    	return eventService.getResult();
    	
    }
}
