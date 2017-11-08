package com.lotus.ante.api;

import com.lotus.ante.customexceptions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.lotus.ante.dao.*;
import com.lotus.ante.domain.Bet;
import com.lotus.ante.domain.Competitor;
import com.lotus.ante.domain.Event;
import com.lotus.ante.domain.User;
import com.lotus.ante.validator.Validator;

@Path("admin")
public class AdminAPI {
	static boolean activeConnection = false;
	private final static boolean LOGIN = true;
	private final static boolean LOGOUT = false;
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response user() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
		jsonObject.put("success", true);
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("user/create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createCustomer(@FormParam("username") String username,
								   @FormParam("password") String password,
								   @FormParam("firstname") String firstname,
								   @FormParam("lastname") String lastname) throws JSONException {
		
		JSONObject jsonObject = new JSONObject();	
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
		try {
			UserDAO userDao = new UserOJDBDAO();
			Validator.validateUsername(username);
			Validator.validatePassword(password);
			Validator.validateName(firstname);
			Validator.validateName(lastname);
			userDao.createCustomer(username, password, firstname, lastname);
		} catch (SQLIntegrityConstraintViolationException | UsernameException | PasswordException | NameException e) {
			return response(jsonObject, e);
		}	
		
		LoginAPI.resetSession();
		jsonObject.put("success", true);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("event")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showEvents() throws JsonGenerationException, JsonMappingException, IOException {
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
		EventDAO eventDao = new EventOJDBDAO();
		List<Event> eventList = eventDao.listEvents();
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
	
	@Path("event/{eventtype}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showEvents(@PathParam("eventtype") String eventType) throws JsonGenerationException, JsonMappingException, IOException {
		eventType = eventType.toUpperCase();
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
		EventDAO eventDao = new EventOJDBDAO();
		ObjectMapper mapper = new ObjectMapper();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
		mapper.setDateFormat(sdf);
		List<Event> eventList = eventDao.listEvents(eventType);
		String response = "{}";
		if(!eventList.isEmpty()) {
			response = mapper.writeValueAsString(eventList);
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}
	
	@Path("event/create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createEvent(@FormParam("eventcode") String eventCode,
								@FormParam("eventdate") String eventDate,
								@FormParam("eventtype") String eventType,
								@FormParam("competitor1") String comp1,
								@FormParam("competitor2") String comp2) throws JSONException {
		
		eventCode = eventCode.toUpperCase();
		eventType = eventType.toUpperCase();
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
		EventDAO eventDao = new EventOJDBDAO();
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		try {
			Validator.validateName(comp1);
			Validator.validateName(comp2);
			Validator.validateCode(eventCode);
			Validator.validateEventType(eventType);
			eventDao.createEvent(eventCode, eventDate, eventType);
		} catch (SQLIntegrityConstraintViolationException | ParseException | EventCodeException | EventTypeException | DateException | NameException e) {
			return response(jsonObject, e);
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
		
		LoginAPI.resetSession();
		jsonObject.put("success", true);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("event/result/specify")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response specifyWinner(@FormParam("eventcode")String eventCode, @FormParam("winner")String winner) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		EventDAO eventDao = new EventOJDBDAO();
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		eventCode = eventCode.toUpperCase();
		winner = winner.toUpperCase();
		
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}

		try {
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
			return response(jsonObject, e);
		}
		
		LoginAPI.resetSession();
		jsonObject.put("success", true);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("event/result/{eventcode}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response viewResult(@PathParam("eventcode") String eventCode) {
		eventCode = eventCode.toUpperCase();
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
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
			}
			
			jsonObject.put("Date", event.getEventDate());
			jsonObject.put("Competitors", event.getCompetitors());
			jsonObject.put("Result",event.getResult());
			jsonObject.put("isSettled", event.isEventSettled());
			
		} catch (EventCodeException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("user/adjust")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response adjustBalance(@FormParam("username") String username, @FormParam("amount") String amount) {
		JSONObject jsonObject = new JSONObject();
		UserDAO userDao = new UserOJDBDAO();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		try {
			Validator.validateUsername(username);
			User customer = userDao.getCustomer(username);
			
			if(customer == null) {
				jsonObject.put("success", false);
				jsonObject.put("errorMessage", "User does not exist.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			BigDecimal newBalance = Validator.validateBalance(amount, customer);
			customer.setBalance(newBalance);
			userDao.updateBalance(customer);
		} catch (NumberFormatException | BalanceException | UsernameException e) {
			return response(jsonObject, e);
		}
		
		LoginAPI.resetSession();
		jsonObject.put("success", true);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("bet/show")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showBets() throws JsonGenerationException, JsonMappingException, IOException {
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
		BetDAO betDao = new BetOJDBDAO();
		List<Bet> betList = betDao.listBet();
		ObjectMapper mapper = new ObjectMapper();
		String response = "{}";
		if(!betList.isEmpty()) {
			response = mapper.writeValueAsString(betList);
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}
	
	@Path("bet/show/{eventcode}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showBets(@PathParam("eventcode") String eventCode) throws JsonGenerationException, JsonMappingException, IOException {
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
		eventCode = eventCode.toUpperCase();
		BetDAO betDao = new BetOJDBDAO();
		List<Bet> betList = betDao.listBet(eventCode);
		ObjectMapper mapper = new ObjectMapper();
		String response = "{}";
		if(!betList.isEmpty()) {
			response = mapper.writeValueAsString(betList);
		} 
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}

	@Path("bet/show/user/{username}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showBetsByUser(@PathParam("username") String username) throws JsonGenerationException, JsonMappingException, IOException {
		JSONObject jsonObject = new JSONObject();
		UserDAO userDao = new UserOJDBDAO();
		BetDAO betDao = new BetOJDBDAO();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(jsonObject, e);
		}
		
		User user = userDao.getCustomer(username);
		
		if(user == null) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", "User does not exist.");
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
		List<Bet> betList = betDao.listBet(user.getUserId());
		ObjectMapper mapper = new ObjectMapper();
		String response = "{}";
		if(!betList.isEmpty()) {
			response = mapper.writeValueAsString(betList);
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}

	private void checkUserType() throws AccountTypeException{
		if(activeConnection == LOGOUT) {
			throw new AccountTypeException("Invalid account privileges.");
		}
	}
	
	private Response responseForbidden(JSONObject jsonObject, Exception e) {
		jsonObject.put("success", false);
		jsonObject.put("errorMessage", e.getMessage());
		return Response.status(403).entity(jsonObject.toString()).build();
	}

	private Response response(JSONObject jsonObject, Exception e) {
		jsonObject.put("success", false);
		jsonObject.put("errorMessage", e.getMessage());
		return Response.status(200).entity(jsonObject.toString()).build();
	}
}