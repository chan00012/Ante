package com.lotus.ante.api;

import java.io.IOException;
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
import com.lotus.ante.customexceptions.EventTypeException;
import com.lotus.ante.customexceptions.SessionExpiredException;
import com.lotus.ante.dao.*;
import com.lotus.ante.domain.Event;
import com.lotus.ante.validators.Validator;
import com.lotus.ante.ENUMSCONST.*;

import oracle.jdbc.dcn.DatabaseChangeEvent.EventType;

@Path("customer")
public class CustomerAPI {
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
		
		return Response.status(200).entity(jsonList.toString()).build();
	}
	
	public void checkUserType() throws AccountTypeException{
		if(activeConnection == LOGOUT) {
			throw new AccountTypeException("Invalid account privileges.");
		}
	}
}
