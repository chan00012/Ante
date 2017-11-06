package com.lotus.ante.domain;

import java.util.*;

public class Event {
	
	boolean isEventSettled;
	boolean isEventDone;
	boolean isEventDraw;
	String eventCode;
	String eventType;
	String result;
	List<Competitor> competitors;
	Competitor winner;
	Date eventDate;
	

	public boolean isEventSettled() {
		return isEventSettled;
	}
	public void setEventSettled(boolean isEventSettled) {
		this.isEventSettled = isEventSettled;
	}
	public boolean isEventDone() {
		return isEventDone;
	}
	public void setEventDone(boolean isEventDone) {
		this.isEventDone = isEventDone;
	}
	public boolean isEventDraw() {
		return isEventDraw;
	}
	public void setEventDraw(boolean isEventDraw) {
		this.isEventDraw = isEventDraw;
	}
	public String getEventCode() {
		return eventCode;
	}
	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public List<Competitor> getCompetitors() {
		return competitors;
	}
	public void setCompetitors(List<Competitor> competitors) {
		this.competitors = competitors;
	}
	public Competitor getWinner() {
		return winner;
	}
	public void setWinner(Competitor winner) {
		this.winner = winner;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	
	

}
