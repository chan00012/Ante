package com.lotus.ante.dao;
import com.lotus.ante.domain.*;

public interface UserDAO {

	User getUser(String username, String password);
	
}
