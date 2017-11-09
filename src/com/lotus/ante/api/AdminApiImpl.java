package com.lotus.ante.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import com.lotus.ante.customexceptions.BalanceException;
import com.lotus.ante.customexceptions.DateException;
import com.lotus.ante.customexceptions.EventCodeException;
import com.lotus.ante.customexceptions.EventTypeException;
import com.lotus.ante.customexceptions.NameException;
import com.lotus.ante.customexceptions.PasswordException;
import com.lotus.ante.customexceptions.UsernameException;
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

public abstract class AdminApiImpl {
	
	protected Response createCustomer(String username, String password, String firstname, String lastname) {
		try {
			Validator.validateUsername(username);
			Validator.validatePassword(password);
			Validator.validateName(firstname);
			Validator.validateName(lastname);
			UserDAO userDao = new UserOJDBDAO();
			userDao.createCustomer(username, password, firstname, lastname);
		} catch (SQLIntegrityConstraintViolationException | UsernameException | PasswordException | NameException e) {
			return responseFail(e);
		}	
		return responseSuccess();
	}
	
	protected Response adjustBalance(String username, String amount) throws UsernameException, BalanceException {
		JSONObject jsonObject = new JSONObject();
		Validator.validateUsername(username);
		UserDAO userDao = new UserOJDBDAO();
		User customer = userDao.getCustomer(username);
		
		if(customer == null) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", "User does not exist.");
			return Response.status(200).entity(jsonObject.toString()).build();
		} else {
			BigDecimal newBalance = Validator.validateBalance(amount, customer);
			customer.setBalance(newBalance);
			userDao.updateBalance(customer);
			return responseSuccess();
		}
	}
	
	protected Response createEvent(String eventCode, String eventDate, String eventType, String comp1, String comp2) {
		JSONObject jsonObject = new JSONObject();
		EventDAO eventDao = new EventOJDBDAO();
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		try {
			eventCode = eventCode.toUpperCase();
			eventType = eventType.toUpperCase();
			Validator.validateName(comp1);
			Validator.validateName(comp2);
			Validator.validateCode(eventCode);
			Validator.validateEventType(eventType);
			eventDao.createEvent(eventCode, eventDate, eventType);
		} catch (SQLIntegrityConstraintViolationException | ParseException | EventCodeException | EventTypeException | DateException | NameException e) {
			return responseFail(e);
		}
		
		try {
			competitorDao.createCompetitor(comp1, eventCode);
			competitorDao.createCompetitor(comp2, eventCode);
		} catch (SQLIntegrityConstraintViolationException e) {
			eventDao.deleteEvent(eventCode);
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
		return responseSuccess();
	}
	
	protected Response specifyWinner(String eventCode, String winner) {
		try {
			JSONObject jsonObject = new JSONObject();
			EventDAO eventDao = new EventOJDBDAO();
			CompetitorDAO competitorDao = new CompetitorOJDBDAO();
			
			eventCode = eventCode.toUpperCase();
			winner = winner.toUpperCase();
			Validator.validateCode(eventCode);
			Event event = eventDao.retrieveEvent(eventCode);
			Validator.validateEventDate(event);
			
			if(winner.compareToIgnoreCase("DRAW") == 0) {
				event.setResult("DRAW");
				event.setEventDraw(true);
			} else {		
				Competitor competitor = competitorDao.retrieveCompetitor(eventCode, winner);
				if(competitor == null) {
					jsonObject.put("success", false);
					jsonObject.put("errorMessage", "Invalid competitor");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				event.setWinner(competitor);
				event.setResult(event.getWinner().getCompetitorName() + " WINS");
			}
			event.setEventDone(true);
			eventDao.persist(event);
			
		} catch(EventCodeException | DateException e) {
			return responseFail(e);
		}
		
		return responseSuccess();
	}
	
	protected Response viewResult(String eventCode, JSONObject jsonObject) {
		try {
			EventDAO eventDao = new EventOJDBDAO();
			ObjectMapper mapper = new ObjectMapper();
			SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
			mapper.setDateFormat(sdf);
			Validator.validateCode(eventCode);
			Event event = eventDao.retrieveEvent(eventCode);
			
			if(event == null) {
				jsonObject.put("success", false);
				jsonObject.put("errorMessage", "Event doesn't exist.");
				return Response.status(200).entity(jsonObject.toString()).build();
			} else {		
				jsonObject.put("Date", event.getEventDate());
				jsonObject.put("Competitors", event.getCompetitors());
				jsonObject.put("Result",event.getResult());
				jsonObject.put("isSettled", event.isEventSettled());
				LoginAPI.resetSession();
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
		} catch (EventCodeException e) {
			return responseFail(e);
		}
	}
	
	protected Response showBetList(List<Bet> betList) throws IOException, JsonGenerationException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		String response = "{}";
		if(!betList.isEmpty()) {
			response = mapper.writeValueAsString(betList);
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}
	
	protected Response showEventList(List<Event> eventList)
			throws IOException, JsonGenerationException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
		mapper.setDateFormat(sdf);	
		String response = "{}";
		if(!eventList.isEmpty()) {
			response = mapper.writeValueAsString(eventList);
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}
	
	protected Response showCustomerList(List<User> customerList)
			throws IOException, JsonGenerationException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		String response = "{}";
		if(!customerList.isEmpty()) {
			response = mapper.writeValueAsString(customerList);
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}

	protected Response responseForbidden(Exception e) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("success", false);
		jsonObject.put("errorMessage", e.getMessage());
		return Response.status(403).entity(jsonObject.toString()).build();
	}

	protected Response responseFail(Exception e) {
		LoginAPI.resetSession();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("success", false);
		jsonObject.put("errorMessage", e.getMessage());
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	protected Response responseSuccess() {
		JSONObject jsonObject = new JSONObject();
		LoginAPI.resetSession();
		jsonObject.put("success", true);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	

}
