package com.lotus.ante.validators;

import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.lotus.ante.ENUMSCONST.EventType;
import com.lotus.ante.customexceptions.*;
import com.lotus.ante.domain.Event;

public final class Validator {
	
	private Validator() {
		
	}
	
	public static void validateName(String name) throws NameException {
		
		if(name.isEmpty()){
			throw new NameException("Name can't be empty.");
		}
		
		if(name.length() > 20) {
			throw new NameException("Name exceeded 20 characters.");
		}
		
		if(!StringUtils.isAlpha(name)) {
			throw new NameException("Name must be alpha only.");
		}
	}
	
	public static void validateUsername(String username) throws UsernameException {
		
		if(username.isEmpty()) {
			throw new UsernameException("Username can't be empty.");
		}
		
		if(username.length()>10) {
			throw new UsernameException("Username must not exceed 10 characters.");
		}
		
		if(username.contains(" ")) {
			throw new UsernameException("Username must not contain spaces.");
		}
		
		if(!StringUtils.isAlphanumeric(username)) {
			throw new UsernameException("Username must not contain special characters.");
		}
	}
	
	public static void validatePassword(String password) throws PasswordException {
		
		if(password.isEmpty()) {
			throw new PasswordException("Password can't be empty.");
		}
			
		if(password.length() > 20) {
			throw new PasswordException("Password must not exceed 20 characters.");
		}
		
		if(password.contains(" ")) {
			throw new PasswordException("Password must not contain spaces.");
		}
		
		
	}
	
	public static void validateCode(String eventCode) throws EventCodeException {
		if(eventCode.length() != 5) {
			throw new EventCodeException("Event code must be 5 character.");
		}
		
		if(!StringUtils.isAlphanumeric(eventCode)) {
			throw new EventCodeException("Event code must only be alphanumeric.");
		}
		
		if(eventCode.contains(" ")) {
			throw new EventCodeException("Event code must not contain space.");
		}
		
		if(eventCode.isEmpty()) {
			throw new EventCodeException("Event code can't be empty.");
		}
	}

	public static void validateEventType(String eventType) throws EventTypeException {
		if(eventType.isEmpty()) {
			throw new EventTypeException("Event type can't be empty.");
		}
		
		try {
			EventType.valueOf(eventType);
		} catch(RuntimeException e) {
			throw new EventTypeException("Event type doesn't exist.");
		}
	}

	public static void validateInputDate(Date eventDate) throws DateException {
		if(eventDate.before(new Date())) {
			throw new DateException("Date already passed.");
		}
	}
	
	public static void validateEventDate(Event event)throws DateException {
		if(event.isEventDone() == true) {
			throw new DateException("Event is already done.");
		}
		if(event.getEventDate().compareTo(new Date()) > 0) {
			throw new DateException("Event not yet started.");
		}
	}
	
	public static String randomCharGenerator() {
		
		final String alphabet = "Q1W2E3R4T5Y6U7I8O9P0LKJHGFDSAZXCVBNM";
		final int limit = alphabet.length();
		Random rand = new Random();
		String randomChar = "";
		
		for(int i = 0; i<5; i++) {
			randomChar += alphabet.charAt(rand.nextInt(limit));
		}
		
		return randomChar;
	}

	public static boolean checkIfDouble(String str) {
		int decIndicator = 0;
		
		for(int i = 0; i<str.length(); i++) {
			if(str.charAt(i) == '.' && decIndicator <= 0) {
				decIndicator++;
				continue;
			}
			else if(str.charAt(i) == '.' && decIndicator > 0) {
				return false;				
			}
		
			if(str.charAt(i) < '0' || str.charAt(i) > '9') {
				return false;
			}
		}
		return true;
	}

}
