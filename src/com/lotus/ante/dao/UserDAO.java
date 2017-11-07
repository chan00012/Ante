package com.lotus.ante.dao;
import com.lotus.ante.domain.*;
import java.sql.SQLIntegrityConstraintViolationException;

public interface UserDAO {

	User getUser(String username, String password);
	void createCustomer(String username, String password, String firstname, String lastname) throws SQLIntegrityConstraintViolationException;
	
}
