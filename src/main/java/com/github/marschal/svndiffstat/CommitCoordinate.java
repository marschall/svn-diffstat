package com.github.marschal.svndiffstat;

import java.util.Date;

final class CommitCoordinate {

	private final long revision;
	private final Date date;

	CommitCoordinate(long revision, Date date) {
		this.revision = revision;
		this.date = date;
	}
	
	long getRevision() {
		return revision;
	}
	
	Date getDate() {
		return date;
	}
	
}