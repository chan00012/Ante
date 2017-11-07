package com.lotus.ante.dao;

import java.sql.*;

import com.lotus.ante.domain.User;

public class UserOJDBDAO implements UserDAO {
	
	public UserOJDBDAO() {
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
	
	@Override
 	public User getUser(String username, String password) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		User user = null;
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ? ");
			statement.setString(1, username);
			statement.setString(2, password);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				user = new User();
				user.setUserId(rs.getLong("user_id"));
				user.setAccountType(rs.getBoolean("type"));
				user.setUserName(rs.getString("username"));
				user.setFirstName(rs.getString("firstname"));
				user.setLastName(rs.getString("lastname"));
				user.setBalance(rs.getBigDecimal("balance"));
			}
			rs.close();	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return user;
	}

	@Override
	public void createCustomer(String username, String password, String firstname, String lastname)
			throws SQLIntegrityConstraintViolationException {
		
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("INSERT INTO users(user_id,username,password,firstname,lastname) VALUES (user_sq.NEXTVAL,?,?,?,?)");
			statement.setString(1, username);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			statement.executeUpdate();
		} catch(SQLIntegrityConstraintViolationException e){
			throw new SQLIntegrityConstraintViolationException("Account already exist.");
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


	
}
