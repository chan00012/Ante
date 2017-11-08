package com.lotus.ante.dao;
import com.lotus.ante.customexceptions.AccountTypeException;
import com.lotus.ante.domain.*;
import java.sql.SQLIntegrityConstraintViolationException;

public interface UserDAO {

	User getUser(String username, String password) throws AccountTypeException;
	User getCustomer(String username) throws AccountTypeException;
	User getCustomer(long userId);
	void updateBalance(User customer);
	void createCustomer(String username, String password, String firstname, String lastname) throws SQLIntegrityConstraintViolationException;
	
	
}
