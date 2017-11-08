package com.lotus.ante.dao;
import com.lotus.ante.customexceptions.AccountTypeException;
import com.lotus.ante.domain.*;
import java.sql.SQLIntegrityConstraintViolationException;

public interface UserDAO {

	void createCustomer(String username, String password, String firstname, String lastname) throws SQLIntegrityConstraintViolationException;
	void updateBalance(User customer);
	User getUser(String username, String password);
	User getCustomer(String username);
	User getCustomer(long userId);
	
	
}
