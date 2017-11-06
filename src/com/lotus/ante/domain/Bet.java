package com.lotus.ante.domain;

import java.math.BigDecimal;

public class Bet {

	long userId;
	long betId;
	String eventCode;
	String transactionCode;
	Competitor competitorSelected;
	BigDecimal betAmount;
	BigDecimal winnings;
	

	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getBetId() {
		return betId;
	}
	public void setBetId(long betId) {
		this.betId = betId;
	}
	public String getEventCode() {
		return eventCode;
	}
	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}
	public String getTransactionCode() {
		return transactionCode;
	}
	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}
	public Competitor getCompetitorSelected() {
		return competitorSelected;
	}
	public void setCompetitorSelected(Competitor competitorSelected) {
		this.competitorSelected = competitorSelected;
	}
	public BigDecimal getBetAmount() {
		return betAmount;
	}
	public void setBetAmount(BigDecimal betAmount) {
		this.betAmount = betAmount;
	}
	public BigDecimal getWinnings() {
		return winnings;
	}
	public void setWinnings(BigDecimal winnings) {
		this.winnings = winnings;
	}
	
	
}
