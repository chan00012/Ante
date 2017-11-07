package com.lotus.ante.ENUMSCONST;

public enum SportType {
	
	FOOT("FOOTBALL"),
	BASK("BASKETBALL"),
	BOXI("BOXING"),
	TENN("TENNIS");
	
	private String sport;
	
	SportType(String sport) {
		this.sport = sport;
	}

	public String getSport() {
		return sport;
	}
}
