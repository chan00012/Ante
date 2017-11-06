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

@Path("admin")
public class AdminAPI {
	static boolean activeConnection = false;
	
	
	@Path("yolo")
	@GET
	public Response yolo() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		
		LoginAPI.loginSession();
		LoginAPI.checkSessionTime();
		if(activeConnection == false) {
			return Response.status(403).entity(jsonObject.toString()).build();
		}
		
		try {
			jsonObject.put("success", true);
		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("errorMessage", "Cannot save data.");
		}
		
		LoginAPI.resetSession();
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
}
