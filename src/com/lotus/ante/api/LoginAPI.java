package com.lotus.ante.api;

import com.lotus.ante.customexceptions.*;
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

import com.lotus.ante.dao.*;
import com.lotus.ante.domain.User;

@Path("login")
public class LoginAPI {
	static Date sessionTime = new Date();	
	private final static boolean ADMIN = true;
	private final static boolean CUSTOMER = false;
	private final static boolean LOGIN = true;
	private final static boolean LOGOUT = false;
	private final static long FIVEMINUITES = 300000;
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response login(@FormParam("username") String username,@FormParam("password") String password) {
		JSONObject jsonObject = new JSONObject();
		
		UserDAO userDao = new UserOJDBDAO();
		User user = null;
		try {
			sessionTime = new Date();
			user = userDao.getUser(username, password);
			if(user.getAccountType() == ADMIN) {
				AdminAPI.activeConnection = LOGIN;
				CustomerAPI.activeConnection = LOGOUT;
				jsonObject.put("success", true);
				
			} else if (user.getAccountType() == CUSTOMER)  {
				CustomerAPI.activeConnection = LOGIN;
				AdminAPI.activeConnection = LOGOUT;
				jsonObject.put("success", true);
				
			}
		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", "Invalid account.");
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
	}

	static void checkSessionTime() throws SessionExpiredException {
		if(new Date().getTime() - sessionTime.getTime() >= FIVEMINUITES) {
			CustomerAPI.activeConnection =LOGOUT;
			AdminAPI.activeConnection = LOGOUT;
			throw new SessionExpiredException("Session timeout.");
		}
		resetSession();
	}
	
	static void resetSession() {
		sessionTime = new Date();
	}

}
