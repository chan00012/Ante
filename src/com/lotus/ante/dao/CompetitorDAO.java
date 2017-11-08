package com.lotus.ante.dao;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import com.lotus.ante.customexceptions.CompetitorException;
import com.lotus.ante.domain.*;

public interface CompetitorDAO {
	
	void createCompetitor(String competitor, String eventCode)throws SQLIntegrityConstraintViolationException;
	void deleteCompetitors(String eventCode);
	List<Competitor> listCompetitor(String eventCode);
	Competitor retrieveCompetitor(String eventCode, String winner) throws CompetitorException;
	Competitor retrieveCompetitor(long competitorId) throws CompetitorException;

}
