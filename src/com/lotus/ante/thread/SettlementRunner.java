package com.lotus.ante.thread;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.lotus.ante.api.CustomerAPI;
import com.lotus.ante.customexceptions.AccountTypeException;
import com.lotus.ante.dao.*;
import com.lotus.ante.domain.*;
import com.lotus.ante.api.*;

public class SettlementRunner extends Thread {
	private final static String WIN = "WIN";
	private final static String LOSE = "LOSE";
	private final static String DRAW = "DRAW";
	private final static long FIVEMINUITES = 10000; //300000
	
	
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(FIVEMINUITES);
				settlement();
			} catch (InterruptedException | AccountTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
	}
	
	private void settlement() throws AccountTypeException {
		EventDAO eventDao = new EventOJDBDAO();
		BetDAO betDao = new BetOJDBDAO();
		
		List<Event> eventList = eventDao.listEvents();
		List<Bet> betList = new ArrayList<>();
		for(Event event : eventList) {
			if(event.isEventDone() == true && event.isEventSettled() == false) {
				betList = betDao.listBet(event.getEventCode());
				checkBets(betList, event, betDao);
				event.setEventSettled(true);
			}
		}
	}

	private void checkBets(List<Bet> betList, Event event, BetDAO betDao) throws AccountTypeException {
		for(Bet bet : betList) {
			long chooseCompId = bet.getCompetitorSelected().getCompetitorId();
			long winnerId = event.getWinner().getCompetitorId();
			UserDAO userDao = new UserOJDBDAO();
			User customer = userDao.getCustomer(bet.getUserId());
			settlementDecision(event, bet, chooseCompId, winnerId, customer);
			userDao.updateBalance(customer);
			betDao.persist(bet);
		}
	}

	private void settlementDecision(Event event, Bet bet, long chooseCompId, long winnerId, User customer) {
		if(event.isEventDraw() == true) {
			bet.setStatus(DRAW);
			bet.setWinnings(bet.getBetAmount());
			customer.setBalance(customer.getBalance().add(bet.getWinnings()));
		}

		if(chooseCompId == winnerId) {
			bet.setWinnings(bet.getBetAmount().multiply(new BigDecimal("2")));
			customer.setBalance(customer.getBalance().add(bet.getWinnings()));
			bet.setStatus(WIN);
		} else {
			bet.setWinnings(BigDecimal.ZERO);
			bet.setStatus(LOSE);
		}
	}
}

