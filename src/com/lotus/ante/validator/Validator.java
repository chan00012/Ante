package com.lotus.ante.validator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.lotus.ante.ENUMSCONST.SportType;
import com.lotus.ante.customexceptions.*;
import com.lotus.ante.domain.Event;
import com.lotus.ante.domain.User;

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
		
		if(!StringUtils.isAlphaSpace(name)) {
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
			SportType.valueOf(eventType);
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
		if(event.getEventDate().after(new Date())) {
			throw new DateException("Event not yet started.");
		}
	}

	public static void validateBetDate(Event event) throws DateException {
		if(event.getEventDate().before(new Date())) {
			throw new DateException("Event already started.");
		}
	}
	
	public static BigDecimal validateBalance(String input, User customer) throws BalanceException {
		
		BigDecimal balance = checkIfDouble(input);
		BigDecimal customerBalance = customer.getBalance();
		if(customerBalance.add(balance).compareTo(BigDecimal.ZERO) == -1) {
			throw new BalanceException("Negative balance.");
		}
		else {
			return customerBalance.add(balance);			
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
	
	public static BigDecimal validateStake(String amount, User customer) throws BalanceException {
		
		BigDecimal min = new BigDecimal("100.00");
		BigDecimal max = new BigDecimal("1000.00");
		BigDecimal customerBalance = customer.getBalance();
		
		BigDecimal stake = checkIfDouble(amount);
		
		if(stake.compareTo(min) < 0 || stake.compareTo(max) > 0) {
			throw new BalanceException("Stake must be on range [100.00,1000.00]");
		}
		
		if(customerBalance.subtract(stake).compareTo(BigDecimal.ZERO) == -1) {
			throw new BalanceException("Balance not enough.");
		}
		
		customer.setBalance(customerBalance.subtract(stake));
		return stake;
	}

	private static BigDecimal checkIfDouble(String str) throws BalanceException {
		int decIndicator = 0;
		
		if(str.isEmpty()) {
			throw new BalanceException("Amount can't be empty.");
		}
		
		for(int i = 0; i<str.length(); i++) {
			if(str.charAt(i) == '.' && decIndicator <= 0) {
				decIndicator++;
				continue;
			}
			else if(str.startsWith("-") || str.startsWith("+")) {
				continue;
			}
			else if(str.charAt(i) == '.' && decIndicator > 0) {
				throw new BalanceException("Invalid amount.");				
			}
		
			if(str.charAt(i) < '0' || str.charAt(i) > '9') {
				throw new BalanceException("Invalid amount.");	
			}
		}
		
		BigDecimal balance = new BigDecimal(str);
		balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
		return balance;
	}
}
