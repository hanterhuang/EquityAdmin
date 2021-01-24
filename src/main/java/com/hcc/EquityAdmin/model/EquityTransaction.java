package com.hcc.EquityAdmin.model;

import lombok.Data;

@Data
public class EquityTransaction {
	private long transactionId;
	private long tradeId;
	private int version;
	private String securityCode;
	private int quantity;
	private String actionType;  // INSERT/UPDATE/CANCEL
	private String bizType;     // Buy/Sell
}
