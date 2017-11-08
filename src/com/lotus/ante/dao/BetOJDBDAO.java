package com.lotus.ante.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.lotus.ante.customexceptions.CompetitorException;
import com.lotus.ante.domain.Bet;
import com.lotus.ante.domain.Competitor;
import com.lotus.ante.domain.Event;
import com.lotus.ante.domain.User;
import com.lotus.ante.validator.Validator;

public class BetOJDBDAO implements BetDAO {
	
	public BetOJDBDAO() {
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
	public void placeBet(User customer, Event event, Competitor competitor, BigDecimal stake) throws SQLIntegrityConstraintViolationException {
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("INSERT INTO bet(bet_id,user_id,event_code,transaction_code,competitor_id,amount_bet,status)"
							+ "VALUES(bet_sq.NEXTVAL,?,?,?,?,?,?)");
			
			statement.setLong(1, customer.getUserId());
			statement.setString(2, event.getEventCode());
			statement.setString(3, Validator.randomCharGenerator());
			statement.setLong(4, competitor.getCompetitorId());
			statement.setBigDecimal(5, stake);
			statement.setString(6, "PENDING");
			statement.executeUpdate();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new SQLIntegrityConstraintViolationException("Existing bet on this event.");
			
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
	public List<Bet> listBet(User customer) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Bet bet = null;
		List<Bet> betList = new ArrayList<>();
		
		try {
			CompetitorDAO competitorDao = new CompetitorOJDBDAO();
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM bet WHERE user_id = ?");
			statement.setLong(1, customer.getUserId());
			rs = statement.executeQuery();
				
			while(rs.next()) {
				bet = new Bet();
				bet.setUserId(rs.getLong("user_id"));
				bet.setBetId(rs.getLong("bet_id"));
				bet.setStatus(rs.getString("status"));
				bet.setEventCode(rs.getString("event_code"));
				bet.setTransactionCode(rs.getString("transaction_code"));
				bet.setCompetitorSelected(competitorDao.retrieveCompetitor(rs.getLong("competitor_id")));
				bet.setBetAmount(rs.getBigDecimal("amount_bet"));
				bet.setWinnings(rs.getBigDecimal("winnings"));		
				betList.add(bet);
			}
		} catch (SQLException | CompetitorException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return betList;
	}

	@Override
	public List<Bet> listBet() {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Bet bet = null;
		List<Bet> betList = new ArrayList<>();
		
		try {
			CompetitorDAO competitorDao = new CompetitorOJDBDAO();
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM bet");
			rs = statement.executeQuery();
				
			while(rs.next()) {
				bet = new Bet();
				bet.setUserId(rs.getLong("user_id"));
				bet.setBetId(rs.getLong("bet_id"));
				bet.setStatus(rs.getString("status"));
				bet.setEventCode(rs.getString("event_code"));
				bet.setTransactionCode(rs.getString("transaction_code"));
				bet.setCompetitorSelected(competitorDao.retrieveCompetitor(rs.getLong("competitor_id")));
				bet.setBetAmount(rs.getBigDecimal("amount_bet"));
				bet.setWinnings(rs.getBigDecimal("winnings"));		
				betList.add(bet);
			}
		} catch (SQLException | CompetitorException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return betList;
	}

	@Override
	public List<Bet> listBet(String eventCode) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Bet bet = null;
		List<Bet> betList = new ArrayList<>();
		
		try {
			CompetitorDAO competitorDao = new CompetitorOJDBDAO();
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM bet WHERE event_code = ?");
			statement.setString(1, eventCode);
			rs = statement.executeQuery();
				
			while(rs.next()) {
				bet = new Bet();
				bet.setUserId(rs.getLong("user_id"));
				bet.setBetId(rs.getLong("bet_id"));
				bet.setStatus(rs.getString("status"));
				bet.setEventCode(rs.getString("event_code"));
				bet.setTransactionCode(rs.getString("transaction_code"));
				bet.setCompetitorSelected(competitorDao.retrieveCompetitor(rs.getLong("competitor_id")));
				bet.setBetAmount(rs.getBigDecimal("amount_bet"));
				bet.setWinnings(rs.getBigDecimal("winnings"));		
				betList.add(bet);
			}
		} catch (SQLException | CompetitorException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return betList;
	}

	@Override
	public List<Bet> listBet(long userId) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Bet bet = null;
		List<Bet> betList = new ArrayList<>();
		
		try {
			CompetitorDAO competitorDao = new CompetitorOJDBDAO();
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM bet WHERE user_id = ?");
			statement.setLong(1, userId);
			rs = statement.executeQuery();
				
			while(rs.next()) {
				bet = new Bet();
				bet.setUserId(rs.getLong("user_id"));
				bet.setBetId(rs.getLong("bet_id"));
				bet.setStatus(rs.getString("status"));
				bet.setEventCode(rs.getString("event_code"));
				bet.setTransactionCode(rs.getString("transaction_code"));
				bet.setCompetitorSelected(competitorDao.retrieveCompetitor(rs.getLong("competitor_id")));
				bet.setBetAmount(rs.getBigDecimal("amount_bet"));
				bet.setWinnings(rs.getBigDecimal("winnings"));		
				betList.add(bet);
			}
		} catch (SQLException | CompetitorException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return betList;
	}

	@Override
	public void persist(Bet bet) {
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("UPDATE bet SET status = ? winnings = ? WHERE bet_id = ?");
			statement.setString(1, bet.getStatus());
			statement.setBigDecimal(2, bet.getWinnings());
			statement.setLong(3, bet.getBetId());
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

}
