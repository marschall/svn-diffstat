package com.github.marschal.svndiffstat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.wc.ISVNDiffGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

import com.github.marschal.svndiffstat.TimeAxisKey.TimeAxisKeyFactory;
import com.github.marschal.svndiffstat.YearMonthWrapper.YearMonthFactory;
import com.github.marschal.svndiffstat.LocalDateWrapper.YearMonthDayFactory;
import com.github.marschal.svndiffstat.YearWeek.YearWeekFactory;


class DiffStatGenerator {


  static NavigableMap<TimeAxisKey, DiffStat> getData(DiffStatConfiguration configuration, ProgressReporter reporter) throws SVNException {
    SVNClientManager clientManager = SVNClientManager.newInstance();

    reporter.startRevisionLogging();
    List<CommitCoordinate> coordinates = getCommitCoordinates(clientManager, configuration, reporter);
    reporter.revisionLoggingDone(coordinates);

    reporter.startRevisionParsing();
    Map<Long, DiffStat> diffStats = getDiffStats(clientManager, coordinates, configuration, reporter);
    reporter.revisionParsinDone(diffStats);

    reporter.startAggregation();
    removeTooLargeValues(diffStats, configuration);
    NavigableMap<TimeAxisKey, DiffStat> aggregatedDiffstats = buildAggregatedDiffstats(coordinates, diffStats);
    insertZeroDataPoints(aggregatedDiffstats);
    reporter.aggregationDone(aggregatedDiffstats);

    return aggregatedDiffstats;
  }
  

  /**
   * If two consecutive data points are more than one tick away add one or tow
   * zero data points between them.
   * 
   * This makes the chart "spikier" but avoids boxes during large periods of inactivity.
   */
  static void insertZeroDataPoints(NavigableMap<TimeAxisKey, DiffStat> diffStats) {
    if (diffStats.size() < 2) {
      return;
    }
    TimeAxisKey previousKeyInMap = diffStats.firstKey();
    TimeAxisKey nextKeyInMap = diffStats.higherKey(previousKeyInMap);
    while (nextKeyInMap != null) {
      TimeAxisKey nextKeyInRange = previousKeyInMap.next();
      if (!nextKeyInMap.equals(nextKeyInRange)) {
        diffStats.put(nextKeyInRange, new DiffStat(0, 0));
        
        TimeAxisKey previousKeyInRange = nextKeyInMap.previous();
        if (!previousKeyInRange.equals(nextKeyInMap)) {
          diffStats.put(previousKeyInRange, new DiffStat(0, 0));
        }
      }
      
      previousKeyInMap = nextKeyInMap;
      nextKeyInMap = diffStats.higherKey(previousKeyInMap);
    }
  }
  

  private static void removeTooLargeValues(Map<Long, DiffStat> diffStats, DiffStatConfiguration configuration) {
    Iterator<Entry<Long, DiffStat>> entrySetIterator = diffStats.entrySet().iterator();
    while (entrySetIterator.hasNext()) {
      Entry<Long, DiffStat> entry = entrySetIterator.next();
      DiffStat diffStat = entry.getValue();
      if (diffStat.added() > configuration.getMaxChanges() || diffStat.removed() > configuration.getMaxChanges()) {
        entrySetIterator.remove();
      }
    }
  }

  private static NavigableMap<TimeAxisKey, DiffStat> buildAggregatedDiffstats(List<CommitCoordinate> coordinates, Map<Long, DiffStat> diffStats) {
    if (coordinates.isEmpty()) {
      return new TreeMap<>();
    }

    TimeAxisKeyFactory factory = getFactory(coordinates);

    Map<Long, TimeAxisKey> revisionToDateMap = buildRevisionToDateMap(coordinates, factory);

    TimeAxisKey fakeStart = factory.fromDate(coordinates.get(0).getDate()).previous();
    NavigableMap<TimeAxisKey, DiffStat> aggregatedDiffstats = new TreeMap<>();
    aggregatedDiffstats.put(fakeStart, new DiffStat(0, 0));
    for (Entry<Long, DiffStat> entry : diffStats.entrySet()) {
      Long revision = entry.getKey();
      TimeAxisKey timeKey = revisionToDateMap.get(revision);
      DiffStat oldDiffStat = aggregatedDiffstats.get(timeKey);
      DiffStat diffStat = entry.getValue();
      if (oldDiffStat != null) {
        oldDiffStat.add(diffStat);
      } else {
        aggregatedDiffstats.put(timeKey, diffStat);
      }
    }
    return aggregatedDiffstats;
  }

  private static TimeAxisKeyFactory getFactory(List<CommitCoordinate> coordinates) {
    Date first = coordinates.get(0).getDate();
    Date last = coordinates.get(coordinates.size() - 1).getDate();

    if (LocalDateWrapper.daysBetween(first, last) >= 100) {
      if (YearWeek.weeksBetween(first, last) >= 100) {
        return new YearMonthFactory();
      } else {
        return new YearWeekFactory();
      }
    } else {
      return new YearMonthDayFactory();
    }

  }

  private static Map<Long, TimeAxisKey> buildRevisionToDateMap(List<CommitCoordinate> coordinates, TimeAxisKeyFactory factory) {
    Map<Long, TimeAxisKey> revisionToDateMap = new HashMap<>(coordinates.size());
    for (CommitCoordinate commitCoordinate : coordinates) {
      Date date = commitCoordinate.getDate();
      long revision = commitCoordinate.getRevision();
      TimeAxisKey timeAxisKey = factory.fromDate(date);
      revisionToDateMap.put(revision, timeAxisKey);
    }
    return revisionToDateMap;
  }

  private static List<CommitCoordinate> getCommitCoordinates(SVNClientManager clientManager, DiffStatConfiguration configuration, ProgressReporter reporter) throws SVNException {
    boolean stopOnCopy = true;
    boolean discoverChangedPaths = false;
    SVNRevision startRevision = SVNRevision.create(1L);
    SVNRevision endRevision = SVNRevision.HEAD;

    File[] paths = new File[]{configuration.getWorkingCopy()};
    RevisionCollector logHandler = new RevisionCollector(configuration, reporter);
    long limit = Long.MAX_VALUE;
    clientManager.getLogClient().doLog(paths, startRevision, endRevision, stopOnCopy, discoverChangedPaths,
        limit, logHandler);
    return logHandler.getCoordinates();
  }

  private static Map<Long, DiffStat> getDiffStats(SVNClientManager clientManager, List<CommitCoordinate> coordinates, DiffStatConfiguration configuration, ProgressReporter reporter) throws SVNException {
    SVNDiffClient diffClient = clientManager.getDiffClient();

    ResetOutputStream result = new ResetOutputStream();
    DiffStatDiffGenerator diffGenerator = new DiffStatDiffGenerator(diffClient.getDiffGenerator(),
        configuration, reporter, result);
    diffClient.setDiffGenerator(diffGenerator);
    File workingCopy = configuration.getWorkingCopy();
    for (CommitCoordinate coordinate : coordinates) {
      long revision = coordinate.getRevision();
      SVNRevision newRevision = SVNRevision.create(revision);
      SVNRevision oldRevision = SVNRevision.create(revision - 1L);
      doDiff(diffClient, result, workingCopy, newRevision, oldRevision);
    }

    return diffGenerator.getDiffStats();
  }


  private static void doDiff(SVNDiffClient diffClient, ResetOutputStream result, File workingCopy, SVNRevision newRevision, SVNRevision oldRevision) throws SVNException {
    SVNDepth depth = SVNDepth.INFINITY;
    boolean useAncestry = true;
    //diffClient.setGitDiffFormat(true);
    Collection<String> changeLists = null;
    diffClient.doDiff(workingCopy, oldRevision, workingCopy, newRevision, depth, useAncestry, result, changeLists);
  }
  

  static String getExtension(String path) {
    int lastIndex = lastIndexOf('.', path);
    if (lastIndex == -1 || lastIndex == path.length() - 1) {
      return null;
    }
    return path.substring(lastIndex + 1, path.length());
  }
  

  private static int lastIndexOf(char c, String s) {
    int lastIndex = s.indexOf(c);
    if (lastIndex == -1) {
      return lastIndex;
    }
    while (true) {
      int nextIndex = s.indexOf(c, lastIndex + 1);
      if (nextIndex == -1) {
        return lastIndex;
      }
      lastIndex = nextIndex;
    }

  }


  static final class RevisionCollector implements ISVNLogEntryHandler {

    private final Set<String> authors;
    private final List<CommitCoordinate> coordinates;
    private final ProgressReporter reporter;

    RevisionCollector(DiffStatConfiguration configuration, ProgressReporter reporter) {
      this.authors = configuration.getAuthors();
      this.reporter = reporter;
      this.coordinates = new ArrayList<>();
    }


    @Override
    public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
      long revision = logEntry.getRevision();
      String logEntryAuthor = logEntry.getAuthor();
      if (this.authors.contains(logEntryAuthor)) {
        Date date = logEntry.getDate();
        CommitCoordinate coordinate = new CommitCoordinate(revision, date);
        this.coordinates.add(coordinate);
      }
      this.reporter.revisionLogged(revision);
    }

    List<CommitCoordinate> getCoordinates() {
      return this.coordinates;
    }

  }

  static final class DiffStatDiffGenerator implements ISVNDiffGenerator {

    private final ISVNDiffGenerator delegate;
    private final Map<Long, DiffStat> diffStats;
    private final Set<String> includedFileExtensions;
    private final ProgressReporter reporter;
    private final ResetOutputStream output;

    DiffStatDiffGenerator(ISVNDiffGenerator delegate, DiffStatConfiguration configuration, ProgressReporter reporter, ResetOutputStream output) {
      this.delegate = delegate;
      this.includedFileExtensions = configuration.getIncludedFiles();
      this.reporter = reporter;
      this.output = output;
      this.diffStats = new HashMap<>();
    }

    Map<Long, DiffStat> getDiffStats() {
      return this.diffStats;
    }

    void clearDiffStats() {
      this.diffStats.clear();
    }

    @Override
    public void init(String anchorPath1, String anchorPath2) {
      this.delegate.init(anchorPath1, anchorPath2);
    }

    @Override
    public void setBasePath(File basePath) {
      this.delegate.setBasePath(basePath);
    }

    @Override
    public void setForcedBinaryDiff(boolean forced) {
      this.delegate.setForcedBinaryDiff(forced);
    }

    @Override
    public boolean isForcedBinaryDiff() {
      return this.delegate.isForcedBinaryDiff();
    }

    @Override
    public void setEncoding(String encoding) {
      this.delegate.setEncoding(encoding);
      this.output.setEncoding(encoding);
    }

    @Override
    public String getEncoding() {
      return this.delegate.getEncoding();
    }

    @Override
    public void setEOL(byte[] eol) {
      this.delegate.setEOL(eol);
      this.output.setEOL(eol);
    }

    @Override
    public byte[] getEOL() {
      return this.delegate.getEOL();
    }

    @Override
    public void setDiffDeleted(boolean isDiffDeleted) {
      this.delegate.setDiffDeleted(isDiffDeleted);
    }

    @Override
    public boolean isDiffDeleted() {
      return this.delegate.isDiffDeleted();
    }

    @Override
    public void setDiffAdded(boolean isDiffAdded) {
      this.delegate.setDiffAdded(isDiffAdded);
    }

    @Override
    public boolean isDiffAdded() {
      return this.delegate.isDiffAdded();
    }

    @Override
    public void setDiffCopied(boolean isDiffCopied) {
      this.delegate.setDiffCopied(isDiffCopied);
    }

    @Override
    public boolean isDiffCopied() {
      return this.delegate.isDiffCopied();
    }

    @Override
    public void setDiffUnversioned(boolean diffUnversioned) {
      this.delegate.setDiffUnversioned(diffUnversioned);

    }

    @Override
    public boolean isDiffUnversioned() {
      return this.delegate.isDiffUnversioned();
    }

    @Override
    public File createTempDirectory() throws SVNException {
      return this.delegate.createTempDirectory();
    }

    @Override
    public void displayPropDiff(String path, SVNProperties baseProps, SVNProperties diff, OutputStream result) throws SVNException {
    }

    @Override
    public void displayFileDiff(String path, File file1, File file2, String rev1, String rev2, String mimeType1, String mimeType2, OutputStream result) throws SVNException {
      long newRevision = Long.parseLong(rev2.substring("(revision ".length(), rev2.length() - 1));
      if (this.considerFile(path)) {
        DiffStat diffStat;
        if (file1 == null) {
          int added = this.countLines(file2);
          diffStat = new DiffStat(added, 0);
        } else if (file2 == null ) {
          int removed = this.countLines(file1);
          diffStat = new DiffStat(0, removed);
        } else {
          diffStat = this.parseNormal(path, file1, file2, rev1, rev2, mimeType1, mimeType2, result, newRevision);
        }
        this.addDiffStat(newRevision, diffStat);
      }
      this.reporter.revisionParsed(newRevision);
    }
    
    private int countLines(File file) throws SVNException {
      try (InputStream fileInput = new FileInputStream(file);
          BufferedReader reader = new BufferedReader(new InputStreamReader(fileInput))) {
        int count = 0;
        while (reader.readLine() != null) {
          count += 1;
        }
        return count;
      } catch (IOException e) {
        SVNErrorCode errorCode = SVNErrorCode.IO_ERROR;
        SVNErrorMessage message = SVNErrorMessage.create(errorCode, "could not count lines");
        throw new SVNException(message, e);
      }
    }

    private DiffStat parseNormal(String path, File file1, File file2, String rev1, String rev2, String mimeType1, String mimeType2, OutputStream result, long newRevision) throws SVNException {
      ResetOutputStream resetOutStream = (ResetOutputStream) result;
      resetOutStream.initialize();
      this.delegate.displayFileDiff(path, file1, file2, rev1, rev2, mimeType1, mimeType2, result);
      return resetOutStream.finish();
    }

    private void addDiffStat(Long revision, DiffStat diffStat) {
      DiffStat oldStat = this.diffStats.get(revision);
      if (oldStat != null) {
        oldStat.add(diffStat);
      } else {
        this.diffStats.put(revision, diffStat);
      }
    }

    private boolean considerFile(String path) {
      if (path == null || path.isEmpty()) {
        return false;
      }
      String extension = getExtension(path);
      return extension != null && this.includedFileExtensions.contains(extension);
    }

    @Override
    public void displayDeletedDirectory(String path, String rev1, String rev2) throws SVNException {
    }

    @Override
    public void displayAddedDirectory(String path, String rev1, String rev2) throws SVNException {
    }


  }

}
