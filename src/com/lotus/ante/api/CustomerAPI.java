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

import com.lotus.ante.customexceptions.AccountTypeException;
import com.lotus.ante.customexceptions.SessionExpiredException;
import com.lotus.ante.dao.*;
import com.lotus.ante.domain.User;
import com.lotus.ante.domain.Bet;
import com.lotus.ante.ENUMSCONST.*;


@Path("customer")
public class CustomerAPI extends CustomerApiImpl {
	static boolean activeConnection = false;
	static User currCustomer = null;
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
			return responseForbidden(e);
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
	public Response requestShowEventsByType(@PathParam("eventtype") String eventType) throws JsonGenerationException, JsonMappingException, IOException {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
	
		return showEventsByType(eventType);
	}

	@Path("bet")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response requestBet(@FormParam("eventcode") String eventCode,
						@FormParam("competitor") String comp,
						@FormParam("stake") String stake) {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		return bet(eventCode, comp, stake, currCustomer);
	}

	@Path("bet/show")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response showBet() throws JsonGenerationException, JsonMappingException, IOException {
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		BetDAO betDao = new BetOJDBDAO();
		List<Bet> betList = betDao.listBet(currCustomer);
		return showBetlist(betList);
	}

	@Path("balance")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response requestBalanceInquiry() {
		
		try {
			LoginAPI.checkSessionTime();
			checkUserType();
			
		} catch (SessionExpiredException | AccountTypeException e) {
			return responseForbidden(e);
		}
		
		UserDAO userDao = new UserOJDBDAO();
		User customer = userDao.getCustomer(currCustomer.getUserId());
		return showBalance(customer);
	}

	private void checkUserType() throws AccountTypeException {
		if (activeConnection == LOGOUT) {
			throw new AccountTypeException("Invalid account privileges.");
		}
	}

}
