package com.lotus.ante.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import java.util.ArrayList;
import java.util.List;

import com.lotus.ante.domain.Competitor;

public class CompetitorOJDBDAO implements CompetitorDAO {
	
	public CompetitorOJDBDAO() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to establish database connection");
		}
	}
	
	private static Connection getConnection() throws SQLException {
		Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "ante",
				"password");
		connection.setAutoCommit(false);
		return connection;
	}
	
	public void createCompetitor(String competitor, String eventCode) throws SQLIntegrityConstraintViolationException {
		competitor = competitor.toUpperCase();
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("INSERT INTO competitor(competitor_id,competitor_name,event_code) VALUES(competitor_sq.NEXTVAL,?,?)") ;
			statement.setString(1, competitor);
			statement.setString(2, eventCode);
			statement.executeUpdate();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new SQLIntegrityConstraintViolationException("Competitors must have different names.");
		} finally {
			try {
				connection.commit();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void deleteCompetitors(String eventCode) {
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("DELETE FROM competitor WHERE event_code = ?");
			statement.setString(1, eventCode);
			statement.executeUpdate();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				connection.commit();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public List<Competitor> listCompetitor(String eventCode) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Competitor competitor = null;
		List<Competitor> competitorList = new ArrayList<>();
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM competitor WHERE event_code = ?");
			statement.setString(1, eventCode);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				competitor = extractCompetitor(rs);
				competitorList.add(competitor);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return competitorList;
	}

	@Override
	public Competitor retrieveCompetitor(String eventCode, String winner){
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Competitor competitor = null;
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM competitor WHERE event_code = ? AND competitor_name = ?");
			statement.setString(1, eventCode);
			statement.setString(2,winner);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				competitor = extractCompetitor(rs);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return competitor;
	}

	@Override
	public Competitor retrieveCompetitor(long competitorId) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Competitor competitor = null;
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM competitor WHERE competitor_id = ?");
			statement.setLong(1, competitorId);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				competitor = extractCompetitor(rs);
			}
					
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return competitor;
	}
	
	private Competitor extractCompetitor(ResultSet rs) throws SQLException {
		Competitor competitor;
		competitor = new Competitor();
		competitor.setCompetitorId(rs.getLong("competitor_id"));
		competitor.setCompetitorName(rs.getString("competitor_name"));
		return competitor;
	}

}
