package com.hcc.EquityAdmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.hcc.EquityAdmin.model.EquityEventFactory;
import com.hcc.EquityAdmin.model.EquityTransaction;
import com.hcc.EquityAdmin.model.ObjectEvent;
import com.hcc.EquityAdmin.service.EquityEventService;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;


@SpringBootApplication
public class EquityAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(EquityAdminApplication.class, args);
	}
	
	@Bean
	public RingBuffer<ObjectEvent<EquityTransaction>> equityRingBuffer() {
		int bufferSize = 1024;
		
		EquityEventFactory factory= new EquityEventFactory();
		
		Disruptor<ObjectEvent<EquityTransaction>> disruptor = new Disruptor<ObjectEvent<EquityTransaction>>(
		         factory, bufferSize, DaemonThreadFactory.INSTANCE);
		

	    disruptor.handleEventsWith(EquityEventService::handleEvent);
	    
	    // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<ObjectEvent<EquityTransaction>> ringBuffer = disruptor.getRingBuffer();
        return ringBuffer;
	}

}
