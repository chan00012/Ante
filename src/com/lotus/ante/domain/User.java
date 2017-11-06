package com.lotus.ante.domain;

import java.math.BigDecimal;

public class User {
	
	private boolean accountType;
	private long userId;
	private String userName;
	private String firstName;
	private String LastName;
	private String password;
	private BigDecimal balance;
	
	public boolean getAccountType() {
		return accountType;
	}
	public void setAccountType(boolean accountType) {
		this.accountType = accountType;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return LastName;
	}
	public void setLastName(String lastName) {
		LastName = lastName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	
	

}
