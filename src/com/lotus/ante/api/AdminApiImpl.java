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

import com.lotus.ante.customexceptions.AccountTypeException;
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
	
	protected Response createCustomer(String username, String password, String firstname, String lastname, String type) {
		try {
			Validator.validateUsername(username);
			Validator.validatePassword(password);
			Validator.validateName(firstname);
			Validator.validateName(lastname);
			boolean acctType = Validator.validateAccountType(type);
			UserDAO userDao = new UserOJDBDAO();
			userDao.createCustomer(username, password, firstname, lastname, acctType);
		} catch (SQLIntegrityConstraintViolationException | UsernameException | PasswordException | NameException | AccountTypeException e) {
			return responseFail(e);
		}	
		return responseSuccess();
	}
	
	protected Response adjustBalance(String username, String amount) {
		UserDAO userDao = new UserOJDBDAO();
		User customer = userDao.getCustomer(username);
		
		if(customer == null) {
			return responseFail("Customer doesn't Exist");
			
		} else {
			try {
				BigDecimal newBalance;
				newBalance = Validator.validateBalance(amount, customer);
				customer.setBalance(newBalance);
				userDao.updateBalance(customer);
			} catch (NumberFormatException | BalanceException e) {
				return responseFail(e);
			}
			return responseSuccess();
		}
	}
	
	protected Response createEvent(String eventCode, String eventDate, String eventType, String comp1, String comp2) {
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
			return responseFail(e);
		}
		
		return responseSuccess();
	}
	
	protected Response specifyWinner(String eventCode, String winner) {
		try {
			EventDAO eventDao = new EventOJDBDAO();
			
			eventCode = eventCode.toUpperCase();
			winner = winner.toUpperCase();
			Validator.validateCode(eventCode);
			Event event = eventDao.retrieveEvent(eventCode);
			Validator.validateEventDate(event);
			
			if(winner.compareToIgnoreCase("DRAW") == 0) {
				event.setResult("DRAW");
				event.setEventDraw(true);
				event.setEventDone(true);
				eventDao.persist(event);
				return responseSuccess();
			} else {		
				return setWinner(eventCode, winner, event);
			}
			
		} catch(EventCodeException | DateException e) {
			return responseFail(e);
		}
		
	}

	protected Response viewResult(String eventCode ) {
			JSONObject jsonObject = new JSONObject();
			EventDAO eventDao = new EventOJDBDAO();
			Event event = eventDao.retrieveEvent(eventCode);
			SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
			
			if(event == null) {
				return Response.status(200).entity("{}").build();
			} else {	
				String dateStr = sdf.format(event.getEventDate());
				jsonObject.put("Date", dateStr);
				jsonObject.put("Competitors", event.getCompetitors());
				jsonObject.put("Result",event.getResult());
				jsonObject.put("isSettled", event.isEventSettled());
			
				
				LoginAPI.resetSession();
				return Response.status(200).entity(jsonObject.toString()).build();
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
	
	protected Response showEventList(List<Event> eventList) throws IOException, JsonGenerationException, JsonMappingException {
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
	
	protected Response showCustomerList(List<User> customerList) throws IOException, JsonGenerationException, JsonMappingException {
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
	
	protected Response responseFail(String errMsg) {
		LoginAPI.resetSession();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("success", false);
		jsonObject.put("errorMessage", errMsg);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	protected Response responseSuccess() {
		JSONObject jsonObject = new JSONObject();
		LoginAPI.resetSession();
		jsonObject.put("success", true);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	private Response setWinner(String eventCode, String winner, Event event) {
		EventDAO eventDao = new EventOJDBDAO();
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		Competitor competitor = competitorDao.retrieveCompetitor(eventCode, winner);
		if(competitor == null) {
			return responseFail("Invalid competitor.");
		} else {
			event.setWinner(competitor);
			event.setResult(event.getWinner().getCompetitorName() + " WINS");
			event.setEventDone(true);
			eventDao.persist(event);
			return responseSuccess();
		}
	}
	

}
