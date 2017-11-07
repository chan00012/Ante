package com.lotus.ante.api;

import com.lotus.ante.customexceptions.*;

import java.io.IOException;
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
import com.lotus.ante.domain.Event;
import com.lotus.ante.validators.Validator;

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
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
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
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		try {
			UserDAO userDao = new UserOJDBDAO();
			Validator.validateUsername(username);
			Validator.validatePassword(password);
			Validator.validateName(firstname);
			Validator.validateName(lastname);
			userDao.createCustomer(username, password, firstname, lastname);
		} catch (SQLIntegrityConstraintViolationException | UsernameException | PasswordException | NameException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
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
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
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
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		try {
			Validator.validateEventType(eventType);
		} catch (EventTypeException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
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
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		EventDAO eventDao = new EventOJDBDAO();
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		try {
			Validator.validateCode(eventCode);
			Validator.validateEventType(eventType);
			eventDao.createEvent(eventCode, eventDate, eventType);
		} catch (SQLIntegrityConstraintViolationException | ParseException | EventCodeException | EventTypeException | DateException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
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
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		Event event = null;
		EventDAO eventDao = new EventOJDBDAO();
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		
		try {
			eventCode = eventCode.toUpperCase();
			Validator.validateCode(eventCode);
			event = eventDao.retrieveEvent(eventCode);
			Validator.validateEventDate(event);
			if(winner.compareToIgnoreCase("DRAW") == 0) {
				eventDao.persistDraw(event);
			} else {		
				event.setWinner(competitorDao.retrieveCompetitor(eventCode, winner));
				eventDao.persist(event);
			}
			
		} catch(RuntimeException | EventCodeException | DateException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
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
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		EventDAO eventDao = new EventOJDBDAO();
		ObjectMapper mapper = new ObjectMapper();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
		mapper.setDateFormat(sdf);
		try {
			Validator.validateCode(eventCode);
			Event event = eventDao.retrieveEvent(eventCode);
			jsonObject.put("Date", event.getEventDate());
			jsonObject.put("Competitors", event.getCompetitors());
			jsonObject.put("Result",event.getResult());
			jsonObject.put("isSettled", event.isEventSettled());	
			LoginAPI.resetSession();
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (RuntimeException | EventCodeException e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", e.getMessage());
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	public void checkUserType() throws AccountTypeException{
		if(activeConnection == LOGOUT) {
			throw new AccountTypeException("Invalid account privileges.");
		}
	}
}