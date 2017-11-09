package com.lotus.ante.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
				user = extractCustomer(rs);
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
	public void createCustomer(String username, String password, String firstname, String lastname, boolean acctType)
			throws SQLIntegrityConstraintViolationException {
		
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("INSERT INTO users(user_id,username,password,firstname,lastname,type) VALUES (user_sq.NEXTVAL,?,?,?,?,?)");
			statement.setString(1, username);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			statement.setBoolean(5, acctType);
			statement.executeUpdate();
		} catch(SQLIntegrityConstraintViolationException e){
			throw new SQLIntegrityConstraintViolationException("Username already exist.");
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
	public User getCustomer(String username) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		User user = null;
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND type = 0 ");
			statement.setString(1, username);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				user = extractCustomer(rs);
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
	public void updateBalance(User customer) {
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("UPDATE users SET balance = ? Where username = ?");
			statement.setBigDecimal(1, customer.getBalance());
			statement.setString(2, customer.getUserName());
			statement.executeUpdate();
			
		} catch (SQLException e) {
			try {
				e.printStackTrace();
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
	public User getCustomer(long userId) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		User user = null;
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM users WHERE user_id = ? AND type = 0 ");
			statement.setLong(1, userId);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				user = extractCustomer(rs);
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
	
	private User extractCustomer(ResultSet rs) throws SQLException {
		User user;
		user = new User();
		user.setUserId(rs.getLong("user_id"));
		user.setAccountType(rs.getBoolean("type"));
		user.setUserName(rs.getString("username"));
		user.setFirstName(rs.getString("firstname"));
		user.setLastName(rs.getString("lastname"));
		user.setBalance(rs.getBigDecimal("balance"));
		return user;
	}

	@Override
	public List<User> listCustomer() {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		User customer = null;
		List<User> customerList = new ArrayList<>();
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			rs = statement.executeQuery("SELECT * FROM users WHERE type = 0");
			
			while(rs.next()) {
				customer = extractCustomerExPass(rs);
				customerList.add(customer);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return customerList;
	}


	@Override
	public List<User> listCustomer(String query) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		User customer = null;
		List<User> customerList = new ArrayList<>();
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("SELECT * FROM users WHERE type = 0 AND (firstname = ? OR lastname = ? or username = ?)");
			statement.setString(1, query);
			statement.setString(2, query);
			statement.setString(3, query);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				customer = extractCustomerExPass(rs);
				customerList.add(customer);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return customerList;
	}
	
	private User extractCustomerExPass(ResultSet rs) throws SQLException {
		User customer;
		customer = new User();
		customer.setUserId(rs.getLong("user_id"));
		customer.setUserName(rs.getString("username"));
		customer.setFirstName(rs.getString("firstname"));
		customer.setLastName(rs.getString("lastname"));
		customer.setAccountType(rs.getBoolean("type"));
		customer.setBalance(rs.getBigDecimal("balance"));
		return customer;
	}
}
