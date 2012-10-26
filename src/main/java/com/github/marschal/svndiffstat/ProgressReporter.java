package com.github.marschal.svndiffstat;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;


final class ProgressReporter {
	
	private final PrintStream out;
	private long loggingStart;
	private long parsingStart;
	private long aggregationStart;
	private long lastSeenRevision;
	
	
	ProgressReporter(PrintStream out) {
		this.out = out;
		this.lastSeenRevision = 0L;
	}
	
	void startRevisionLogging() {
		this.loggingStart = System.currentTimeMillis();
	}
	
	void revisionLoggingDone(List<CommitCoordinate> coordinates) {
		long loggingEnd = System.currentTimeMillis();
		this.out.printf("parsed: %d revisions is %ds%n", coordinates.size(), (loggingEnd - this.loggingStart) / 1000);
	}

	void revisionLogged(long revision) {
		
	}
	
	void startRevisionParsing() {
		this.parsingStart = System.currentTimeMillis();
	}
	
	void revisionParsinDone(Map<Long, DiffStat> diffStats) {
		long parsingEnd = System.currentTimeMillis();
		this.out.printf("parsed %d commits in %ds%n", diffStats.size(), (parsingEnd - this.parsingStart) / 1000);
		
		DiffStat total = new DiffStat(0, 0);
		for (DiffStat diffStat : diffStats.values()) {
			total.add(diffStat);
		}
		this.out.println("total: " + total);
	}
	
	void revisionParsed(long revision) {
		if (this.lastSeenRevision != revision) {
			
		}
	}

	void startAggregation() {
		this.aggregationStart = System.currentTimeMillis();
	}

	void aggregationDone(SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats) {
		long aggregationEnd = System.currentTimeMillis();
		this.out.printf("aggregated into %d data poiints %ds%n", aggregatedDiffstats.size(), (aggregationEnd - this.aggregationStart) / 1000);
	}

}
