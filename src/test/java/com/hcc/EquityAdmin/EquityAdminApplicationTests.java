package com.hcc.EquityAdmin;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.hcc.EquityAdmin.model.EquityTransaction;
import com.hcc.EquityAdmin.service.EquityEventService;
import com.hcc.EquityAdmin.util.TransferTrade;

@SpringBootTest
class EquityAdminApplicationTests{
	
	@Value("${action.type}")
	private String actionType;
	
	@Value("${biz.type}")
	private String bizType;
	
	// wait ms in case the disruptor consumer does not finish yet
	int nano = 100;
	
	@Autowired
	private EquityEventService eventService;

	// valid transaction input
	@Test
	void transcationUnderRule()  throws Exception{
		eventService.clear();
		String[] trans = {
							"1-1-1-REL-50-INSERT-Buy", 
							"2-2-1-ITC-40-INSERT-Sell",
							"3-3-1-INF-70-INSERT-Buy",
							"4-1-2-REL-60-UPDATE-Buy",
							"5-2-2-ITC-30-CANCEL-Buy",
							"6-4-1-INF-20-INSERT-Sell"
						 };
		for(String transaction: trans) {
			EquityTransaction et = TransferTrade.toEquityTransction(transaction, actionType, bizType);
			eventService.doTransaction(et);
		}
		Thread.sleep(nano);
		ConcurrentHashMap<String, Integer> result = eventService.getResult();
		
		Assertions.assertTrue(result.get("REL") == 60);
		Assertions.assertTrue(result.get("ITC") == 0);
		Assertions.assertTrue(result.get("INF") == 50);
	}
	
	// valid transaction input
	@Test
	void transcationUnderRule2()  throws Exception{
		eventService.clear();
		String[] trans = {
							"1-1-1-REL-50-INSERT-Buy", 
							"2-2-1-ITC-40-INSERT-Sell",
							"3-3-1-INF-70-INSERT-Buy",
							"4-1-2-REL-60-UPDATE-Buy",
							"5-2-2-ITC-30-CANCEL-Buy",
							"6-4-1-INF-20-INSERT-Sell",
							"7-4-2-INF-20-UPDATE-Sell",
							"8-1-3-REL-40-UPDATE-Sell",
							"9-4-3-INF-80-UPDATE-Buy"
						 };
		for(String transaction: trans) {
			EquityTransaction et = TransferTrade.toEquityTransction(transaction, actionType, bizType);
			eventService.doTransaction(et);
		}
		Thread.sleep(nano);
		ConcurrentHashMap<String, Integer> result = eventService.getResult();
		
		Assertions.assertTrue(result.get("REL") == -40);
		Assertions.assertTrue(result.get("ITC") == 0);
		Assertions.assertTrue(result.get("INF") == 80);
	}
	
	/*
	 * break the rule: INSERT will always be 1st version of a Trade, CANCEL will always be last version of Trade.
	 * the first version of a trade is UPDATE rather than INSERT
	 *  
	 */
	@Test
	void transactionWithoutInsertFirst() throws Exception{
		eventService.clear();
		String[] trans = {
							"1-1-1-REL-50-INSERT-Buy", 
							"2-2-1-ITC-40-INSERT-Sell",
							"3-3-1-INF-70-INSERT-Buy",
							"4-2-1-REL-60-UPDATE-Buy",   //tradeId 2 securityCode REL is new combination, but first version is UPDATE, break rule;
							"5-2-2-ITC-30-CANCEL-Buy",
							"6-4-1-INF-20-INSERT-Sell"
						 };
		for(String transaction: trans) {
			EquityTransaction et = TransferTrade.toEquityTransction(transaction, actionType, bizType);
			eventService.doTransaction(et);
		}
		Thread.sleep(nano);
		ConcurrentHashMap<String, Integer> result = eventService.getResult();
		
		Assertions.assertTrue(result.get("REL") == 50);
		Assertions.assertTrue(result.get("ITC") == 0);
		Assertions.assertTrue(result.get("INF") == 50);

	}
	
	/*
	 * break the rule: INSERT will always be 1st version of a Trade, CANCEL will always be last version of Trade.
	 *  
	 */
	@Test
	void transactionWithoutCancelLast() throws Exception{
		eventService.clear();
		String[] trans = {
							"1-1-1-REL-50-INSERT-Buy", 
							"2-2-1-ITC-40-INSERT-Sell",
							"3-3-1-INF-70-INSERT-Buy",
							"4-1-2-REL-60-UPDATE-Buy",   
							"5-2-2-ITC-30-CANCEL-Buy",
							"6-4-3-ITC-20-UPDATE-Sell"  // UPDATE version great than CANCEL transaction(the previous one), break the rule, ignore
						 };
		for(String transaction: trans) {
			EquityTransaction et = TransferTrade.toEquityTransction(transaction, actionType, bizType);
			eventService.doTransaction(et);
		}
		Thread.sleep(nano);
		ConcurrentHashMap<String, Integer> result = eventService.getResult();
		
		Assertions.assertTrue(result.get("REL") == 60);
		Assertions.assertTrue(result.get("ITC") == 0);
		Assertions.assertTrue(result.get("INF") == 70);

	}
	
	/*
	 * break the rule: INSERT / UPDATE / CANCEL are actions on a Trade (with same trade id but different version)
	 *  
	 */
	@Test
	void transactionWithSameTradeIdAndVersion() throws Exception{
		eventService.clear();
		String[] trans = {
							"1-1-1-REL-50-INSERT-Buy", 
							"2-2-1-ITC-40-INSERT-Sell",
							"3-3-1-INF-70-INSERT-Buy",
							"4-1-1-REL-60-UPDATE-Buy",   //tradeId 1 securityCode REL combination coming again, but with same version, should ignore;
							"5-2-2-ITC-30-CANCEL-Buy",
							"6-4-1-INF-20-INSERT-Sell"
						 };
		for(String transaction: trans) {
			EquityTransaction et = TransferTrade.toEquityTransction(transaction, actionType, bizType);
			eventService.doTransaction(et);
		}
		Thread.sleep(nano);
		ConcurrentHashMap<String, Integer> result = eventService.getResult();
		
		Assertions.assertTrue(result.get("REL") == 50);
		Assertions.assertTrue(result.get("ITC") == 0);
		Assertions.assertTrue(result.get("INF") == 50);

	}
	
	/*
	 * break the rule: INSERT / UPDATE / CANCEL are actions on a Trade (with same trade id but different version)
	 *  
	 */
	@Test
	void transactionWithSameTradeIdAndVersion2() throws Exception{
		eventService.clear();
		String[] trans = {
							"1-1-1-REL-50-INSERT-Buy", 
							"2-2-1-ITC-40-INSERT-Sell",
							"3-3-1-INF-70-INSERT-Buy",
							"4-1-1-REL-60-UPDATE-Buy",   //tradeId 1 securityCode REL combination coming again, but with same version, should ignore;
							"5-2-2-ITC-30-CANCEL-Buy",
							"6-4-1-INF-20-INSERT-Sell",
							"7-4-1-INF-30-INSERT-Buy"    // same version as the previous transaction, break the rule should ignore
						 };
		for(String transaction: trans) {
			EquityTransaction et = TransferTrade.toEquityTransction(transaction, actionType, bizType);
			eventService.doTransaction(et);
		}
		Thread.sleep(nano);
		ConcurrentHashMap<String, Integer> result = eventService.getResult();
		
		Assertions.assertTrue(result.get("REL") == 50);
		Assertions.assertTrue(result.get("ITC") == 0);
		Assertions.assertTrue(result.get("INF") == 50);

	}
	
	// Break rule
	@Test
	void transcationBreakRule()  throws Exception{
		eventService.clear();
		String[] trans = {
							"1-1-1-REL-50-INSERT-Buy", 
							"2-2-1-ITC-40-INSERT-Sell",
							"3-3-1-INF-70-INSERT-Buy",
							"4-1-2-REL-60-UPDATE-Buy",
							"5-2-2-ITC-30-CANCEL-Buy",
							"6-4-1-INF-20-INSERT-Sell",  
							"7-4-2-INF-20-INSERT-Sell", // break rule, ignore
							"8-1-3-REL-10-UPDATE-Sell",  
							"9-2-2-ITC-30-UPDATE-Buy"   // break rule, ignore
						 };
		for(String transaction: trans) {
			EquityTransaction et = TransferTrade.toEquityTransction(transaction, actionType, bizType);
			eventService.doTransaction(et);
		}
		Thread.sleep(nano);
		ConcurrentHashMap<String, Integer> result = eventService.getResult();
		
		Assertions.assertTrue(result.get("REL") == -10);
		Assertions.assertTrue(result.get("ITC") == 0);
		Assertions.assertTrue(result.get("INF") == 50);
	}

}
