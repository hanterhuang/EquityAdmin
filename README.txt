1) test way:
1. run the springboot EquityAdminApplication class main method to start the service.
2. open the browser, in order to handle the input easily, use "-" to replace the " " to link the input attributes,
	for example, transfer the "1 1 1 REL 50 INSERT Buy" to "1-1-1-REL-50-INSERT-Buy", put the input data at the end of the URL
	http://localhost:18084/equity/trade/come/, e.g. http://localhost:18084/equity/trade/come/1-1-1-REL-50-INSERT-Buy
3. then you will get the output in your browser, and same outcome printed in the eclipse(or IDEA) console, looks like "====== positions outcome: {REL=50}";

2) for unit test:
you need to run the EquityAdminApplicationTests class in JUNIT test, to make sure the result is what you expected.
the test cases contain the valid transaction data sequences and the sequences with invalid data, if the coming transaction data breaks some rule, then just ignore it.


3) requirement analysis:
when i look at the requirement the first eye, it makes me think of disruptor from LMAX, so i just use it, generally it can use a linkedBlockingQueue as buffer for the coming transaction data 
or just handle the transaction directly.
the coming transaction is published as an event in diruptor, see EquityEventService doTransaction() method, then the consumer handler will do statistic job, see the EquityEventService handleEvent(),
i make a tradeMap(see it in EquityEventService) to keep the last transaction, and tradePositionMap(see it in EquityEventService) to keep the position for all securities. when a new transaction comes, 
it will be validated by the rules by comparing with last transaction (same trandId), if the coming transaction pass the validation, the position statistic will be computed, otherwise it just ignores and wait for the next transaction.
notice that the different tradeId may has the same securiteCode, so after the validation, it will check the securityCode for position statistic.

4) more issue:
in real production environment, the persistence of tradeMap and tradePositionMap should be considered, by using redis Hash etc in case of the data loss when project down or restarts.
