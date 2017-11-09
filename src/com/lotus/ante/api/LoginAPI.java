package com.lotus.ante.api;

import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.lotus.ante.dao.*;
import com.lotus.ante.domain.User;
import com.lotus.ante.customexceptions.*;

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
	
			sessionTime = new Date();
		
				user = userDao.getUser(username, password);
				if(user == null) {
					jsonObject.put("success", false);
					jsonObject.put("errorMessage", "Invalid account.");
					return Response.status(203).entity(jsonObject.toString()).build();
				}
				
				if(user.getAccountType() == ADMIN) {
					loginAdmin();
					
				} else if (user.getAccountType() == CUSTOMER)  {
					loginCustomer(user);
				}	
		
		jsonObject.put("success", true);	
		return Response.status(200).entity(jsonObject.toString()).build();
	}

	private void loginCustomer(User user) {
		CustomerAPI.activeConnection = LOGIN;
		AdminAPI.activeConnection = LOGOUT;
		CustomerAPI.currCustomer = user;
	}

	private void loginAdmin() {
		AdminAPI.activeConnection = LOGIN;
		CustomerAPI.activeConnection = LOGOUT;
		CustomerAPI.currCustomer = null;
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
