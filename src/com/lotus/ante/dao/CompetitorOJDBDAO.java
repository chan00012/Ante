package com.lotus.ante.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;

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
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}

}
