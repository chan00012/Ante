package com.lotus.ante.dao;

import java.sql.SQLIntegrityConstraintViolationException;

public interface CompetitorDAO {
	
	void createCompetitor(String competitor, String eventCode)throws SQLIntegrityConstraintViolationException;

}
