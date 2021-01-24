package com.hcc.EquityAdmin.model;

import com.lmax.disruptor.EventFactory;

public class EquityEventFactory implements EventFactory<ObjectEvent<EquityTransaction>> {

	@Override
	public ObjectEvent<EquityTransaction> newInstance() {
		return new ObjectEvent<EquityTransaction>();
	}
	
}
