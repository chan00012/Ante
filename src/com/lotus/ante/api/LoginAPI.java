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

import com.lotus.ante.dao.*;
import com.lotus.ante.domain.User;

@Path("login")
public class LoginAPI {
	static Date sessionTime = null;
	static Date sessionInterval = null;
	
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response login(@FormParam("username") String username,@FormParam("password") String password) {
		JSONObject jsonObject = new JSONObject();
		
		UserDAO userDao = new UserOJDBDAO();
		User user = null;
		try {
			sessionTime = new Date();
			user = userDao.getUser(username, password);
			if(user.getAccountType() == true) {
				AdminAPI.activeConnection = true;
				CustomerAPI.activeConnection = false;
				jsonObject.put("success", true);	
			} else {
				CustomerAPI.activeConnection = true;
				AdminAPI.activeConnection = false;
				jsonObject.put("success", true);
			}
		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", "Invalid account.");
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
	}

	static void checkSessionTime() {
		if(sessionInterval.getTime() - sessionTime.getTime() >= 20000) {
			CustomerAPI.activeConnection = false;
			AdminAPI.activeConnection = false;
			System.out.println("Session expired");
			resetSession();
		}
	}
	static void resetSession() {
		sessionTime = new Date();
	}
	
	static void loginSession() {
		sessionInterval = new Date();
	}
}
