package com.hcc.EquityAdmin.util;

import com.hcc.EquityAdmin.model.EquityTransaction;

public class TransferTrade {
	
	public static EquityTransaction toEquityTransction(String transaction, String actionType, String bizType) throws Exception{
		String[] trans = transaction.split("-");
    	EquityTransaction et = new EquityTransaction();
    	et.setTransactionId(Long.parseLong(trans[0]));
    	et.setTradeId(Long.parseLong(trans[1]));
    	et.setVersion(Integer.parseInt(trans[2]));
    	et.setSecurityCode(trans[3]);
    	et.setQuantity(Integer.parseInt(trans[4]));
    	if(!actionType.contains(trans[5])){
    		throw new Exception("actionType invalid!");
    	}else {
    		et.setActionType(trans[5]);
    	}
    	if(!bizType.contains(trans[6])){
    		throw new Exception("bizType invalid!");
    	}else {
    		et.setBizType(trans[6]);
    	}
    	
    	return et;
	}
}
