package com.github.marschal.svndiffstat;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;


final class ProgressReporter {
	
	private static final long LOG_INTERVAL = 100L;
	
	private final PrintStream out;
	private long loggingStart;
	private long parsingStart;
	private long aggregationStart;
	private long lastParsedRevision;
	private long lastRevisionLoggedTime;
	private long lastRevisionParsedTime;
	
	
	ProgressReporter(PrintStream out) {
		this.out = out;
		this.lastParsedRevision = 0L;
		this.lastRevisionLoggedTime = 0L;
		this.lastRevisionParsedTime = 0L;
	}
	
	void startRevisionLogging() {
		this.loggingStart = System.currentTimeMillis();
	}
	
	void revisionLoggingDone(List<CommitCoordinate> coordinates) {
		long loggingEnd = System.currentTimeMillis();
		long duration = loggingEnd - this.loggingStart;
		if (duration >= 2000) {
			this.out.printf("logged: %d revisions is %ds%n", coordinates.size(), duration / 1000);
		} else {
			this.out.printf("logged: %d revisions is %dms%n", coordinates.size(), duration);
		}
	}

	void revisionLogged(long revision) {
		long now = System.currentTimeMillis();
		if (now - lastRevisionLoggedTime >= LOG_INTERVAL) {
			this.out.println("logged revsion: " + revision);
		}
		this.lastRevisionLoggedTime = now;
	}
	
	void startRevisionParsing() {
		this.parsingStart = System.currentTimeMillis();
	}
	
	void revisionParsinDone(Map<Long, DiffStat> diffStats) {
		long parsingEnd = System.currentTimeMillis();
		long duration = parsingEnd - this.parsingStart;
		if (duration >= 2000) {
			this.out.printf("parsed %d commits in %ds%n", diffStats.size(), duration / 1000);
		} else {
			this.out.printf("parsed %d commits in %dms%n", diffStats.size(), duration);
		}
		
		DiffStat total = new DiffStat(0, 0);
		for (DiffStat diffStat : diffStats.values()) {
			total.add(diffStat);
		}
		this.out.println("total: " + total);
	}
	
	void revisionParsed(long revision) {
		long now = System.currentTimeMillis();
		if (this.lastParsedRevision != revision) {
			if (now - this.lastRevisionParsedTime >= LOG_INTERVAL) {
				this.out.println("parsed revision: " + revision);
			}
			
			this.lastParsedRevision = revision;
			this.lastRevisionParsedTime = now;
		}
	}

	void startAggregation() {
		this.aggregationStart = System.currentTimeMillis();
	}

	void aggregationDone(SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats) {
		long aggregationEnd = System.currentTimeMillis();
		long duration = aggregationEnd - this.aggregationStart;
		if (duration >= 2000) {
			this.out.printf("aggregated into %d data poiints %ds%n", aggregatedDiffstats.size(), duration / 1000);
		} else {
			this.out.printf("aggregated into %d data poiints %dms%n", aggregatedDiffstats.size(), duration);
		}
	}
	
	void saveTo(Path path) {
		this.out.print("Saving to: " + path);
	}
	
	void saveDone() {
		this.out.println(" done");
	}

}
