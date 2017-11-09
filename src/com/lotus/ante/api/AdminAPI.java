package com.lotus.ante.api;


import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
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

import org.json.JSONException;
import org.json.JSONObject;

import com.lotus.ante.dao.*;
import com.lotus.ante.domain.Bet;
import com.lotus.ante.domain.Event;
import com.lotus.ante.domain.User;
import com.lotus.ante.customexceptions.*;

@Path("admin")
public class AdminAPI extends AdminApiImpl {
	static boolean activeConnection = false;
	private final static boolean LOGOUT = false;
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response user() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		jsonObject.put("success", true);
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@Path("customer/create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response requestCreateCustomer(@FormParam("username") String username,
								   		  @FormParam("password") String password,
								   		  @FormParam("firstname") String firstname,
								   		  @FormParam("lastname") String lastname,
								   		  @FormParam("accttype") String type) throws JSONException {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		return createCustomer(username, password, firstname, lastname, type);
	}

	@Path("customer")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response viewCustomers() throws JsonGenerationException, JsonMappingException, IOException {
		
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		UserDAO userDao = new UserOJDBDAO();
		List<User> customerList = userDao.listCustomer();
		
		return showCustomerList(customerList);
		
	}

	@Path("customer/{query}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response viewCustomers(@PathParam("query")String query) throws JsonGenerationException, JsonMappingException, IOException {
		
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		UserDAO userDao = new UserOJDBDAO();
		List<User> customerList = userDao.listCustomer(query);
		return showCustomerList(customerList);
	}
	
	@Path("customer/adjust")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response requestAdjustBalance(@FormParam("username") String username, @FormParam("amount") String amount) {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}

		return adjustBalance(username, amount);
	
	}

	@Path("event")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showEvents() throws JsonGenerationException, JsonMappingException, IOException {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		EventDAO eventDao = new EventOJDBDAO();
		List<Event> eventList = eventDao.listEvents();
		return showEventList(eventList);
	}
	
	@Path("event/{eventtype}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showEvents(@PathParam("eventtype") String eventType) throws JsonGenerationException, JsonMappingException, IOException {
		eventType = eventType.toUpperCase();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		EventDAO eventDao = new EventOJDBDAO();
		List<Event> eventList = eventDao.listEvents(eventType);
		return showEventList(eventList);
	}
	
	@Path("event/create")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response requestCreateEvent(@FormParam("eventcode") String eventCode,
								@FormParam("eventdate") String eventDate,
								@FormParam("eventtype") String eventType,
								@FormParam("competitor1") String comp1,
								@FormParam("competitor2") String comp2) throws JSONException {
		
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		return createEvent(eventCode, eventDate, eventType, comp1, comp2);
	}

	@Path("event/result/specify")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response requestSpecifyWinner(@FormParam("eventcode")String eventCode, @FormParam("winner")String winner) throws JSONException {
		
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}

		return specifyWinner(eventCode, winner);
	}
	
	@Path("event/result/{eventcode}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response requesViewResult(@PathParam("eventcode") String eventCode) {
		eventCode = eventCode.toUpperCase();
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		return viewResult(eventCode);
		
	}

	@Path("bet")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showBets() throws JsonGenerationException, JsonMappingException, IOException {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		BetDAO betDao = new BetOJDBDAO();
		List<Bet> betList = betDao.listBet();
		return showBetList(betList);
	}

	@Path("bet/{eventcode}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showBets(@PathParam("eventcode") String eventCode) throws JsonGenerationException, JsonMappingException, IOException {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		eventCode = eventCode.toUpperCase();
		BetDAO betDao = new BetOJDBDAO();
		List<Bet> betList = betDao.listBet(eventCode);
		return showBetList(betList);
	}

	@Path("bet/user/{username}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showBetsByUser(@PathParam("username") String username) throws JsonGenerationException, JsonMappingException, IOException {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		UserDAO userDao = new UserOJDBDAO();
		User user = userDao.getCustomer(username);
		
		if(user == null) {
			return Response.status(200).entity("{}").build();
		} else {
			BetDAO betDao = new BetOJDBDAO();
			List<Bet> betList = betDao.listBet(user.getUserId());
			return showBetList(betList);
		}
	}

	private void checkUserType() throws AccountTypeException{
		if(activeConnection == LOGOUT) {
			throw new AccountTypeException("Invalid account privileges.");
		}
	}
}