package com.lotus.ante.dao;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import com.lotus.ante.domain.*;

public interface BetDAO {
	void placeBet(User customer, Event event, Competitor c, BigDecimal stake) throws SQLIntegrityConstraintViolationException;
	void persist(Bet bet);
	List<Bet> listBet();
	List<Bet> listBet(String eventCode);
	List<Bet> listBet(long userId);
	List<Bet> listBet(User customer);

}
