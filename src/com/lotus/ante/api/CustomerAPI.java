package com.lotus.ante.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

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

import com.lotus.ante.customexceptions.AccountTypeException;
import com.lotus.ante.customexceptions.BalanceException;
import com.lotus.ante.customexceptions.CompetitorException;
import com.lotus.ante.customexceptions.DateException;
import com.lotus.ante.customexceptions.EventCodeException;
import com.lotus.ante.customexceptions.EventTypeException;
import com.lotus.ante.customexceptions.SessionExpiredException;
import com.lotus.ante.dao.*;
import com.lotus.ante.domain.Competitor;
import com.lotus.ante.domain.Event;
import com.lotus.ante.domain.User;
import com.lotus.ante.domain.Bet;
import com.lotus.ante.validator.Validator;
import com.lotus.ante.ENUMSCONST.*;

import oracle.jdbc.dcn.DatabaseChangeEvent.EventType;

@Path("customer")
public class CustomerAPI {
	static boolean activeConnection = false;
	public static User currCustomer = null;
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
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		
		jsonObject.put("success", true);
		jsonObject.put("username", currCustomer.getUserName());
		jsonObject.put("name", currCustomer.getFirstName() + " " + currCustomer.getLastName());
		jsonObject.put("balance", currCustomer.getBalance());
		
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("sports")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showSports() {
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		for(SportType st : SportType.values()) {
			jsonObject.put(st.toString(), st.getSport());
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonObject.toString()).build();
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
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
	
		EventDAO eventDao = new EventOJDBDAO();
		Date referenceDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
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
	
	@Path("event/bet")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response bet(@FormParam("eventcode") String eventCode,
						@FormParam("competitor") String comp,
						@FormParam("stake") String stake) {
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		try {
			EventDAO eventDao = new EventOJDBDAO();
			CompetitorDAO competitorDao = new CompetitorOJDBDAO();
			BetDAO betDao = new BetOJDBDAO();
			UserDAO userDao = new UserOJDBDAO();
			eventCode = eventCode.toUpperCase();
			comp = comp.toUpperCase();
			
			Event event = eventDao.retrieveEvent(eventCode);
			Validator.validateBetDate(event);
			Competitor competitor = competitorDao.retrieveCompetitor(eventCode, comp);
			BigDecimal placeStake = Validator.validateStake(stake, currCustomer);
			betDao.placeBet(currCustomer, event, competitor, placeStake);
			userDao.updateBalance(currCustomer);
			
		} catch (EventCodeException | BalanceException | DateException | SQLIntegrityConstraintViolationException | CompetitorException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
		}		
	
		jsonObject.put("success", true);
		LoginAPI.resetSession();
		reloadCurrentCustomer();
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("event/bet/show")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showBet() throws JsonGenerationException, JsonMappingException, IOException {
		JSONObject jsonObject = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		BetDAO betDao = new BetOJDBDAO();
		List<Bet> betList = betDao.listBet(currCustomer);
		ObjectMapper mapper = new ObjectMapper();
		String response = "{}";
		if(!betList.isEmpty()) {
			response = mapper.writeValueAsString(betList);
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(response).build();
	}
	
	@Path("balance")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response BalanceInquiry() {
		
		JSONObject jsonObject = new JSONObject();
		JSONObject jsonBalance = new JSONObject();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		reloadCurrentCustomer();
		jsonBalance.put("username", currCustomer.getUserName());
		jsonBalance.put("Balance", currCustomer.getBalance());
	
		
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonBalance.toString()).build();
	}

	private void reloadCurrentCustomer() {
		try {
			UserDAO userDao = new UserOJDBDAO();
			currCustomer = userDao.getCustomer(currCustomer.getUserName());
		} catch (AccountTypeException e) {
			e.printStackTrace();
		}
	}
	
	public void checkUserType() throws AccountTypeException{
		if(activeConnection == LOGOUT) {
			throw new AccountTypeException("Invalid account privileges.");
		}
	}
}
