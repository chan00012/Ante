package com.lotus.ante.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import com.lotus.ante.customexceptions.BalanceException;
import com.lotus.ante.customexceptions.DateException;
import com.lotus.ante.customexceptions.EventCodeException;
import com.lotus.ante.dao.BetDAO;
import com.lotus.ante.dao.BetOJDBDAO;
import com.lotus.ante.dao.CompetitorDAO;
import com.lotus.ante.dao.CompetitorOJDBDAO;
import com.lotus.ante.dao.EventDAO;
import com.lotus.ante.dao.EventOJDBDAO;
import com.lotus.ante.dao.UserDAO;
import com.lotus.ante.dao.UserOJDBDAO;
import com.lotus.ante.domain.Bet;
import com.lotus.ante.domain.Competitor;
import com.lotus.ante.domain.Event;
import com.lotus.ante.domain.User;
import com.lotus.ante.validator.Validator;

public abstract class CustomerApiImpl {
	
	protected Response showEventsByType(String eventType) {
		JSONObject jsonObject = new JSONObject();
		EventDAO eventDao = new EventOJDBDAO();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
		Date referenceDate = new Date();
		eventType = eventType.toUpperCase();
		List<Event> eventList = eventDao.listEvents(eventType);
		List<JSONObject> jsonList = new ArrayList<>();
	
		for(Event event: eventList) {
			if(referenceDate.before(event.getEventDate())) {
				String eventDateStr = sdf.format(event.getEventDate());
				jsonObject.put("date", eventDateStr);
				jsonObject.put("eventCode",event.getEventCode());
				jsonObject.put("description", event.getCompetitors().get(0).getCompetitorName() + " vs. " + event.getCompetitors().get(1).getCompetitorName());
				jsonList.add(jsonObject);
			}
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonList.toString()).build();
	}

	protected Response bet(String eventCode, String comp, String stake, User currCustomer) {
		try {
			EventDAO eventDao = new EventOJDBDAO();
			UserDAO userDao = new UserOJDBDAO();
			eventCode = eventCode.toUpperCase();
			comp = comp.toUpperCase();
			Validator.validateCode(eventCode);
			Event event = eventDao.retrieveEvent(eventCode);
			User customer = userDao.getCustomer(currCustomer.getUserId());
			
			if(event == null) {
				LoginAPI.resetSession();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("success", false);
				jsonObject.put("errorMessage", "Event doesn't exist.");
				return Response.status(200).entity(jsonObject.toString()).build();
				
			} else {	
				return proceedBet(eventCode, comp, stake, event, customer);
			}
			
		} catch (BalanceException | DateException | SQLIntegrityConstraintViolationException | EventCodeException e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
		}		
		
	}

	protected Response responseSuccess() {
		LoginAPI.resetSession();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("success", true);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	protected Response responseForbidden(Exception e) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("success", false);
		jsonObject.put("errorMessage", e.getMessage());
		return Response.status(403).entity(jsonObject.toString()).build();
	}
	
	protected Response showBalance(User customer) {
		JSONObject jsonBalance = new JSONObject();
		jsonBalance.put("username", customer.getUserName());
		jsonBalance.put("Balance", customer.getBalance());
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonBalance.toString()).build();
	}
	
	protected Response showBetlist(List<Bet> betList) throws IOException, JsonGenerationException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		String response = "{}";
		if(!betList.isEmpty()) {
			response = mapper.writeValueAsString(betList);
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}
	
	private Response proceedBet(String eventCode, String comp, String stake, Event event , User customer) throws DateException, BalanceException, SQLIntegrityConstraintViolationException {
		
		Validator.validateBetDate(event);
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		Competitor competitor = competitorDao.retrieveCompetitor(eventCode, comp);
		
		if(competitor == null) {
			LoginAPI.resetSession();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", "Invalid competitor.");
			return Response.status(200).entity(jsonObject.toString()).build();	
		} else {
			return finalizeBet(stake, event, competitor, customer);
		}
	}

	private Response finalizeBet(String stake, Event event, Competitor competitor, User customer) throws BalanceException, SQLIntegrityConstraintViolationException {
		BetDAO betDao = new BetOJDBDAO();
		UserDAO userDao = new UserOJDBDAO();
		BigDecimal placeStake = Validator.validateStake(stake, customer);
		betDao.placeBet(customer, event, competitor, placeStake);
		userDao.updateBalance(customer);
		return responseSuccess();
	}
	
}
