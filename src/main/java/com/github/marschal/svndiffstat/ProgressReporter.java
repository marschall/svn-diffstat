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
  private long loggingEnd;
  private int loggingSize;
  private long parsingStart;
  private long parsingEnd;
  private int parsingSize;
  private long aggregationStart;
  private long aggregationEnd;
  private int aggregationSize;
  private long lastParsedRevision;
  private long lastRevisionLoggedTime;
  private long lastRevisionParsedTime;
  private int totalRevisions;

  ProgressReporter(PrintStream out) {
    this.out = out;
    this.lastParsedRevision = 0L;
    this.lastRevisionLoggedTime = 0L;
    this.lastRevisionParsedTime = 0L;
  }

  void startRevisionLogging() {
    this.loggingStart = System.currentTimeMillis();
  }

  void revisionLoggingDone(List<?> coordinates, int totalRevisions) {
    this.totalRevisions = totalRevisions;
    this.loggingEnd = System.currentTimeMillis();
    this.loggingSize = coordinates.size();
  }

  void revisionLogged(long revision) {
    long now = System.currentTimeMillis();
    if (now - this.lastRevisionLoggedTime >= LOG_INTERVAL) {
      this.out.println("logged revsion: " + revision);
      this.lastRevisionLoggedTime = now;
    }
  }

  void startRevisionParsing() {
    this.parsingStart = System.currentTimeMillis();
  }

  void revisionParsinDone(Map<?, DiffStat> diffStats) {
    this.parsingEnd = System.currentTimeMillis();
    this.parsingSize = diffStats.size();
    

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
        this.lastParsedRevision = revision;
        this.lastRevisionParsedTime = now;
      }
    }
  }

  void startAggregation() {
    this.aggregationStart = System.currentTimeMillis();
  }

  void aggregationDone(SortedMap<?, ?> aggregatedDiffstats) {
    this.aggregationEnd = System.currentTimeMillis();
    this.aggregationSize = aggregatedDiffstats.size();
  }
  
  void finalReport() {
    this.reportLogging();
    this.reportParsing();
    this.reportAggregation();
  }

  private void reportAggregation() {
    long duration = this.aggregationEnd - this.aggregationStart;
    if (duration >= 2000) {
      this.out.printf("aggregated into %d data points %ds%n", this.aggregationSize, duration / 1000);
    } else {
      this.out.printf("aggregated into %d data points %dms%n", this.aggregationSize, duration);
    }
  }

  private void reportParsing() {
    long duration = this.parsingEnd - this.parsingStart;
    if (duration >= 2000) {
      this.out.printf("parsed %d commits in %ds%n", this.parsingSize, duration / 1000);
    } else {
      this.out.printf("parsed %d commits in %dms%n", this.parsingSize, duration);
    }
  }

  private void reportLogging() {
    long duration = this.loggingEnd - this.loggingStart;
    if (duration >= 2000) {
      this.out.printf("logged: %d of %d revisions is %ds%n", this.loggingSize, this.totalRevisions, duration / 1000);
    } else {
      this.out.printf("logged: %d of %d revisions is %dms%n", this.loggingSize, this.totalRevisions, duration);
    }
  }

  void saveTo(Path path) {
    this.out.print("Saving to: " + path);
  }

  void saveDone() {
    this.out.println(" done");
  }

}
