package com.lotus.ante.api;

import java.util.Date;

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
import com.lotus.ante.customexceptions.SessionExpiredException;
import com.lotus.ante.dao.*;

@Path("customer")
public class CustomerAPI {
	static boolean activeConnection = false;
	private final static boolean LOGIN = true;
	private final static boolean LOGOUT = false;
	
	
	@Path("user")
	@GET
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
	
	@Path("user/test")
	@GET
	public Response test() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("success", "here");
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	public void checkUserType() throws AccountTypeException{
		if(activeConnection == LOGOUT) {
			throw new AccountTypeException("Invalid account privileges.");
		}
	}
}
