package com.lotus.ante.dao;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import com.lotus.ante.domain.*;

public interface UserDAO {

	void createCustomer(String username, String password, String firstname, String lastname) throws SQLIntegrityConstraintViolationException;
	void updateBalance(User customer);
	User getUser(String username, String password);
	User getCustomer(String username);
	User getCustomer(long userId);
	List<User> listCustomer();
	List<User> listCustomer(String query);
	
}
