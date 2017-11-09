package com.lotus.ante.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lotus.ante.customexceptions.DateException;
import com.lotus.ante.domain.*;
import com.lotus.ante.validator.Validator;

public class EventOJDBDAO implements EventDAO {
	private final static String PENDING = "PENDING";
	
	public EventOJDBDAO() {
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
	public void createEvent(String eventCode, String eventDate, String eventType) throws ParseException, SQLIntegrityConstraintViolationException, DateException {
		Connection connection = null;
		PreparedStatement statement = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yyyy hh:mm a");
		Date date = sdf.parse(eventDate);
		Validator.validateInputDate(date);
		Timestamp sqlStamp = new Timestamp(date.getTime());
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("INSERT INTO event(event_code,event_date,event_type,result) VALUES(?,?,?,?)");
			statement.setString(1, eventCode);
			statement.setTimestamp(2, sqlStamp);
			statement.setString(3, eventType);
			statement.setString(4, PENDING);
			statement.executeUpdate();
		} catch(SQLIntegrityConstraintViolationException e) {
			throw new SQLIntegrityConstraintViolationException("Event code already exist.");
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
	public void deleteEvent(String eventCode) {
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("DELETE FROM event WHERE event_code = ?");
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
	public List<Event> listEvents() {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		Event event = null;
		List<Event> eventList = new ArrayList<>();
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			rs = statement.executeQuery("SELECT * FROM event");
			
			while(rs.next()) {
				event = extractEvent(rs, competitorDao);
				eventList.add(event);
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
		return eventList;
	}

	@Override
	public List<Event> listEvents(String eventType) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Event event = null;
		List<Event> eventList = new ArrayList<>();
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM event WHERE event_type = ?");
			statement.setString(1, eventType);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				event = extractEvent(rs,competitorDao);
				eventList.add(event);
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
		return eventList;
	}

	@Override
	public Event retrieveEvent(String eventCode){
	
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Event event = null;
		CompetitorDAO competitorDao = new CompetitorOJDBDAO();
		
		try {
			connection = getConnection();
			statement = connection.prepareStatement("SELECT * FROM event WHERE event_code = ?");
			statement.setString(1, eventCode);
			rs = statement.executeQuery();
			
			while(rs.next()) {
				event = extractEvent(rs,competitorDao);
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
		return event;
	}

	@Override
	public void persist(Event event) {
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.
					prepareStatement("UPDATE event SET done = ?, draw = ?, settled = ?, winner_id = ?, result = ? WHERE event_code = ?");
			statement.setBoolean(1, event.isEventDone());
			statement.setBoolean(2, event.isEventDraw());
			statement.setBoolean(3, event.isEventSettled());
			if(event.getWinner() != null) {
				statement.setLong(4, event.getWinner().getCompetitorId());
			} else {
				statement.setLong(4, (0));
			}
			statement.setString(5, event.getResult());
			statement.setString(6, event.getEventCode());
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

	private Event extractEvent(ResultSet rs, CompetitorDAO competitorDao) throws SQLException{
		Event event;
		event = new Event();
		event.setEventCode(rs.getString("event_code"));
		event.setEventDate(rs.getTimestamp("event_date"));
		event.setEventType(rs.getString("event_type"));
		event.setEventDraw(rs.getBoolean("draw"));
		event.setEventDone(rs.getBoolean("done"));
		event.setEventSettled(rs.getBoolean("settled"));
		event.setResult(rs.getString("result"));
		event.setCompetitors(competitorDao.listCompetitor(event.getEventCode()));
		event.setWinner(competitorDao.retrieveCompetitor(rs.getLong("winner_id")));
		return event;
	}
}
