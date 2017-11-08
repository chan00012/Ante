package com.lotus.ante.dao;

import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.util.List;

import com.lotus.ante.customexceptions.DateException;
import com.lotus.ante.customexceptions.EventCodeException;
import com.lotus.ante.domain.*;

public interface EventDAO {
	
	void createEvent(String eventCode, String eventDate, String eventType) throws ParseException, SQLIntegrityConstraintViolationException, DateException;
	void deleteEvent(String eventCode);
	List<Event> listEvents();
	List<Event> listEvents(String eventType);
	Event retrieveEvent(String eventCode) throws EventCodeException;
	void persist(Event event);
}
