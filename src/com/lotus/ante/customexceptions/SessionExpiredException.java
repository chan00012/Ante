package com.lotus.ante.customexceptions;

public class SessionExpiredException extends Exception {

	public SessionExpiredException(String message){
		super(message);
	}
		
}
