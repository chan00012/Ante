package com.lotus.ante.thread;
import java.util.EventListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {
	private SettlementRunner settlementEngine = null;

	@Override
	public void contextDestroyed(ServletContextEvent e) {
		settlementEngine.interrupt();
		
	}

	@Override
	public void contextInitialized(ServletContextEvent e) {
		if((settlementEngine == null) || (settlementEngine.isAlive())) {
			settlementEngine = new SettlementRunner();
			settlementEngine.start();
		}
		
	}

}
