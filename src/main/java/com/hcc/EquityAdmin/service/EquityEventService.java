package com.hcc.EquityAdmin.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcc.EquityAdmin.model.EquityTransaction;
import com.hcc.EquityAdmin.model.ObjectEvent;
import com.lmax.disruptor.RingBuffer;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EquityEventService {
	
	//key: tradeId, value: the new coming transaction
	private final static ConcurrentHashMap<Long, EquityTransaction> tradeMap = new ConcurrentHashMap<>();
	
	// the output map to show the positions
	private final static ConcurrentHashMap<String, Integer> tradePositionMap = new ConcurrentHashMap<>();
	
	@Autowired
	private RingBuffer<ObjectEvent<EquityTransaction>> equityRingBuffer;
	
	public static void handleEvent(ObjectEvent<EquityTransaction> event, long sequence, boolean endOfBatch)
    {
        EquityTransaction comingTransaction = event.getVal();
        //log.info("====== coming transaction: " + comingTransaction);

        Long tradeMapKey = comingTransaction.getTradeId();
        if(tradeMap.containsKey(tradeMapKey)){
        	EquityTransaction lastTransaction = tradeMap.get(tradeMapKey);
        	// validate the coming transaction with the rules first
        	if(validateTransaction(comingTransaction, lastTransaction)) { 
        		if("INSERT".equals(comingTransaction.getActionType())) {
        			tradePositionMap.put(comingTransaction.getSecurityCode(), "Buy".equals(comingTransaction.getBizType()) ? lastTransaction.getQuantity() + comingTransaction.getQuantity() : lastTransaction.getQuantity() - comingTransaction.getQuantity());
            	}else if("UPDATE".equals(comingTransaction.getActionType())) {
            		tradePositionMap.put(comingTransaction.getSecurityCode(), "Buy".equals(comingTransaction.getBizType()) ? comingTransaction.getQuantity() : -comingTransaction.getQuantity());
            	}else if("CANCEL".equals(comingTransaction.getActionType())) {
            		tradePositionMap.put(comingTransaction.getSecurityCode(), 0);
            	}
        	}
        	// update the tradeId key with the latest transaction value
        	tradeMap.put(tradeMapKey, comingTransaction);
        }else { // certain tradeId first comes 
        	if("INSERT".equals(comingTransaction.getActionType())) {
        		tradeMap.put(tradeMapKey, comingTransaction);
            	if(tradePositionMap.containsKey(comingTransaction.getSecurityCode())) {
            		Integer securityLastPosition = tradePositionMap.get(comingTransaction.getSecurityCode());
                	tradePositionMap.put(comingTransaction.getSecurityCode(), "Buy".equals(comingTransaction.getBizType())?securityLastPosition + comingTransaction.getQuantity():securityLastPosition - comingTransaction.getQuantity());
            	}else { // certain securityCode first comes
                	tradePositionMap.put(comingTransaction.getSecurityCode(), "Buy".equals(comingTransaction.getBizType())?comingTransaction.getQuantity():-comingTransaction.getQuantity());
            	}
        	}else {
        		// not validate, do nothing
        	}
        }
        // print position output
        log.info("====== positions outcome: " + tradePositionMap);
    }
	
	private static boolean validateTransaction(EquityTransaction comingTransaction, EquityTransaction lastTransaction) {
		
		// for rule 2, INSERT / UPDATE / CANCEL are actions on a Trade (with same trade id but different version)
		if(comingTransaction.getTradeId() == lastTransaction.getTradeId() && comingTransaction.getVersion() == lastTransaction.getVersion()) {
			return false;
		}
		
		// for rule 3, INSERT will always be 1st version of a Trade, CANCEL will always be last version of Trade.
		String comingActionType = comingTransaction.getActionType();
		Integer comingVersion = comingTransaction.getVersion();
		Integer lastVersion = lastTransaction.getVersion();
		if("INSERT".equals(comingActionType) && comingVersion > lastVersion) {
			return false;
		}
		
		if("CANCEL".equals(comingActionType) && comingVersion < lastVersion) {
			return false;
		}
		
		return true;
	}
	
	public void doTransaction(EquityTransaction et) throws Exception{
		
        long sequence = equityRingBuffer.next();

        try {
        	ObjectEvent<EquityTransaction> event = equityRingBuffer.get(sequence); // Get the entry in the Disruptor
            event.setVal(et);
        } finally {
        	equityRingBuffer.publish(sequence);
        }
	}
	
	// for unit test to reset the maps
	public void clear() {
		tradeMap.clear();
		tradePositionMap.clear();
	}
	
	public ConcurrentHashMap<String, Integer> getResult() {
		return tradePositionMap;
	}
}
