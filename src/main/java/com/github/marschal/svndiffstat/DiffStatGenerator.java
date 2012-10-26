package com.github.marschal.svndiffstat;

import static java.awt.Color.WHITE;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNDiffGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;


public class DiffStatGenerator {
	
	static final Color DATE_LABEL = new Color(0x33, 0x33, 0x33);
	static final Color VALUE_LABEL = new Color(0x88, 0x88, 0x88);
	static final Color DARK_BLUE = new Color(0x04, 0x7D, 0xDA);
	static final Color LIGHT_BLUE = new Color(0x04, 0x7D, 0xDA, 127); // also label
	static final Color DARK_GREY = new Color(0x33, 0x33, 0x33);
	static final Color ADDED_FILL = new Color(0x1D, 0xB3, 0x4F, 127); // also label
	static final Color ADDED_STROKE = new Color(0x1D, 0xB3, 0x4F);
	static final Color REMOVED_FILL = new Color(0xAD, 0x10, 0x17, 127);  // also label
	static final Color REMOVED_STROKE = new Color(0xAD, 0x10, 0x17);

	public static void main(String[] args) throws SVNException {
		System.out.println("!!!current user directory must be working copy!!!");
		long start = System.currentTimeMillis();
		FSRepositoryFactory.setup();
		SVNClientManager clientManager = SVNClientManager.newInstance();
		
		SVNURL repoUrl = SVNURL.parseURIEncoded("file:///Users/marschall/svn/memoryfilesystem");
		boolean stopOnCopy = true;
		boolean discoverChangedPaths = false;
		SVNRevision startRevision = SVNRevision.create(1L);
		SVNRevision endRevision = SVNRevision.HEAD;
		String author = "marschall";
		File workingCopy = new File("/Users/marschall/Documents/workspaces/default/memoryfilesystem-workingcopy");
		File[] paths = new File[]{workingCopy};
		RevisionCollector logHandler = new RevisionCollector(author);
		long limit = Long.MAX_VALUE;
		clientManager.getLogClient().doLog(paths, startRevision, endRevision, stopOnCopy, discoverChangedPaths,
				limit, logHandler);
		List<CommitCoordinate> coordinates = logHandler.getCoordinates();
		
		SVNDiffClient diffClient = clientManager.getDiffClient();
//		Set<String> includedFiles = new HashSet<>(Arrays.asList("java", "xml"));
		Set<String> includedFiles = Collections.singleton("java");
		DiffStatDiffGenerator diffGenerator = new DiffStatDiffGenerator(diffClient.getDiffGenerator(), includedFiles);
		diffClient.setDiffGenerator(diffGenerator);
		SVNDepth depth = SVNDepth.INFINITY;
		boolean useAncestry = true;
//		diffClient.setGitDiffFormat(true);
		Collection<String> changeLists = null;
		OutputStream result = new ResetOutStream();
		for (CommitCoordinate coordinate : coordinates) {
			long revision = coordinate.getRevision();
			SVNRevision newRevision = SVNRevision.create(revision);
			SVNRevision oldRevision = SVNRevision.create(revision - 1L);
			diffClient.doDiff(workingCopy, oldRevision, workingCopy, newRevision, depth, useAncestry, result, changeLists);
		}
		long end = System.currentTimeMillis();
		System.out.printf("%n parsed: %d revisions is %d s%n", coordinates.size(), (end - start) / 1000);
		
		Map<Long, DiffStat> diffStats = diffGenerator.getDiffStats();
		System.out.println(diffStats.size() + " diff stats");
		
		DiffStat total = new DiffStat(0, 0);
		for (DiffStat diffStat : diffStats.values()) {
			total.add(diffStat);
		}
		System.out.println("total: " + total);
		
		Map<Long, YearMonthDay> revisionToDateMap = new HashMap<>(coordinates.size());
		for (CommitCoordinate commitCoordinate : coordinates) {
			Date date = commitCoordinate.getDate();
			long revision = commitCoordinate.getRevision();
			YearMonthDay yearMonthDay = YearMonthDay.fromDate(date);
			revisionToDateMap.put(revision, yearMonthDay);
		}
		YearMonthDay fakeStart = YearMonthDay.fromDate(coordinates.get(0).getDate()).previous();
		
		SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats = new TreeMap<>();
		aggregatedDiffstats.put(fakeStart, new DiffStat(0, 0));
		for (Entry<Long, DiffStat> entry : diffStats.entrySet()) {
			Long revision = entry.getKey();
			YearMonthDay yearMonthDay = revisionToDateMap.get(revision);
			DiffStat oldDiffStat = aggregatedDiffstats.get(yearMonthDay);
			DiffStat diffStat = entry.getValue();
			if (oldDiffStat != null) {
				oldDiffStat.add(diffStat);
			} else {
				aggregatedDiffstats.put(yearMonthDay, diffStat);
			}
		}
		
		JFreeChart chart = createChart(aggregatedDiffstats);
		displayChard(chart);
	}
	
	private static void displayChard(final JFreeChart chart) {
        
        SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				ChartPanel chartPanel = new ChartPanel(chart);
				java.awt.Dimension dimension = new java.awt.Dimension(1200, 600);
				chartPanel.setPreferredSize(dimension);
				chartPanel.setDomainZoomable(true);
				chartPanel.setRangeZoomable(true);
				
				JFrame frame = new JFrame("diff-stat");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setContentPane(chartPanel);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
	
	private static JFreeChart createChart(SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats) {
		
		boolean legend = false;
        boolean tooltips = false;
        boolean urls = false;
        Font helvetica = new Font("Helvetica", Font.PLAIN, 11);
		
		XYDataset dataset = createDeltaDataset("Additions and Delections", aggregatedDiffstats);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("", "", "", dataset, legend, tooltips, urls);
		
		chart.setBackgroundPaint(WHITE);
		chart.setBorderVisible(false);
		
		XYPlot plot = chart.getXYPlot();
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint(WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlineStroke(new BasicStroke(1.0f));
        plot.setRangeGridlinesVisible(false);
        
        plot.setOutlineVisible(false);
        
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("MM/yy"));
        domainAxis.setTickLabelFont(helvetica);

        NumberAxis additionDeletionAxis = (NumberAxis) plot.getRangeAxis(0);
        additionDeletionAxis.setLabel("Additions and Deletions");
        additionDeletionAxis.setLabelFont(helvetica);
        additionDeletionAxis.setTickLabelFont(helvetica);
        additionDeletionAxis.setRangeType(RangeType.FULL);
        additionDeletionAxis.setLowerBound(minimum(aggregatedDiffstats));
        additionDeletionAxis.setUpperBound(maximum(aggregatedDiffstats));
        additionDeletionAxis.setNumberFormatOverride(new AbbreviatingNumberFormat());
        
        XYAreaRenderer areaRenderer = new XYAreaRenderer(XYAreaRenderer.AREA);
        areaRenderer.setOutline(true);
        areaRenderer.setSeriesOutlinePaint(0, ADDED_STROKE);
        areaRenderer.setSeriesOutlineStroke(0, new BasicStroke(1.5f));
        areaRenderer.setSeriesPaint(0, ADDED_FILL);
        areaRenderer.setSeriesOutlinePaint(1, REMOVED_STROKE);
        areaRenderer.setSeriesOutlineStroke(1, new BasicStroke(1.5f));
        areaRenderer.setSeriesPaint(1, REMOVED_FILL);
		plot.setRenderer(0, areaRenderer);
		
		// Total Axix 2
        NumberAxis totalAxis = new NumberAxis("Total Lines");
//        totalAxis.setFixedDimension(10.0);
        totalAxis.setLabelPaint(VALUE_LABEL);
        totalAxis.setTickLabelPaint(DARK_BLUE);
        totalAxis.setLabelFont(helvetica);
        totalAxis.setTickLabelFont(helvetica);
        totalAxis.setNumberFormatOverride(new AbbreviatingNumberFormat());
        plot.setRangeAxis(1, totalAxis);
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        
        XYDataset totalDataSet = createTotalDataset("Total Lines", aggregatedDiffstats);
		plot.setDataset(1, totalDataSet);
        plot.mapDatasetToRangeAxis(1, 1);
//        XYItemRenderer totalRenderer = new XYSplineRenderer();
        XYItemRenderer totalRenderer = new StandardXYItemRenderer();
        totalRenderer.setSeriesPaint(0, LIGHT_BLUE);
        totalRenderer.setSeriesStroke(0, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{6.5f} , 0.0f));
        plot.setRenderer(1, totalRenderer);
		
        return chart;
	}
	
	private static int minimum(SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats) {
		int minimum = Integer.MAX_VALUE;
		for (DiffStat diffStat : aggregatedDiffstats.values()) {
			minimum = min(minimum, -diffStat.removed());
		}
		return minimum + (int) (minimum * 0.10);
	}
	
	private static int maximum(SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats) {
		int maximum = Integer.MIN_VALUE;
		for (DiffStat diffStat : aggregatedDiffstats.values()) {
			maximum = max(maximum, diffStat.added());
		}
		return maximum + (int) (maximum * 0.10);
	}
	
	private static XYDataset createDeltaDataset(String name, SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats) {

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		TimeSeries addedSeries = new TimeSeries(name);
		for (Entry<YearMonthDay, DiffStat> entry : aggregatedDiffstats.entrySet()) {
			YearMonthDay yearMonthDay = entry.getKey();
			DiffStat diffStat = entry.getValue();
			Day day = new Day(yearMonthDay.day(), yearMonthDay.month() + 1, yearMonthDay.year());
			addedSeries.add(day, Integer.valueOf(diffStat.added()));    
		}
		dataset.addSeries(addedSeries);
		
		TimeSeries removedSeries = new TimeSeries(name);
		for (Entry<YearMonthDay, DiffStat> entry : aggregatedDiffstats.entrySet()) {
			YearMonthDay yearMonthDay = entry.getKey();
			DiffStat diffStat = entry.getValue();
			Day day = new Day(yearMonthDay.day(), yearMonthDay.month() + 1, yearMonthDay.year());
			removedSeries.add(day, Integer.valueOf(-diffStat.removed()));    
		}
		dataset.addSeries(removedSeries);


		return dataset;
	}
	
	private static XYDataset createTotalDataset(String name, SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats) {
		int total = 0;
		
		TimeSeries totalSeries = new TimeSeries(name);
		for (Entry<YearMonthDay, DiffStat> entry : aggregatedDiffstats.entrySet()) {
			YearMonthDay yearMonthDay = entry.getKey();
			DiffStat diffStat = entry.getValue();
			Day day = new Day(yearMonthDay.day(), yearMonthDay.month() + 1, yearMonthDay.year());
			total += diffStat.delta();
			totalSeries.add(day, Integer.valueOf(total));    
		}
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(totalSeries);
		
		return dataset;
	}
	
	static final class RevisionCollector implements ISVNLogEntryHandler {
		
		private final String author;
		private final List<CommitCoordinate> coordinates;
		
		RevisionCollector(String author) {
			this.author = author;
			this.coordinates = new ArrayList<>();
		}


		@Override
		public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
			String logEntryAuthor = logEntry.getAuthor();
			if (this.author.equals(logEntryAuthor)) {
				long revision = logEntry.getRevision();
				Date date = logEntry.getDate();
				CommitCoordinate coordinate = new CommitCoordinate(revision, date);
				this.coordinates.add(coordinate);
			}
			
		}
		
		List<CommitCoordinate> getCoordinates() {
			return coordinates;
		}
		
	}
	
	static final class DiffStatDiffGenerator implements ISVNDiffGenerator {
		
		private final ISVNDiffGenerator delegate;
		private final Map<Long, DiffStat> diffStats;
		private final Set<String> includedFileExtensions;
		
		DiffStatDiffGenerator(ISVNDiffGenerator delegate, Set<String> includedFileExtensions) {
			this.delegate = delegate;
			this.includedFileExtensions = includedFileExtensions;
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
		}

		@Override
		public String getEncoding() {
			return this.delegate.getEncoding();
		}

		@Override
		public void setEOL(byte[] eol) {
			this.delegate.setEOL(eol);
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
			if (this.considerFile(path)) {
				ResetOutStream resetOutStream = (ResetOutStream) result;
				resetOutStream.initialize();
				this.delegate.displayFileDiff(path, file1, file2, rev1, rev2, mimeType1, mimeType2, result);
				long newRevision = Long.parseLong(rev2.substring("(revision ".length(), rev2.length() - 1));
				DiffStat diffStat = resetOutStream.finish();
				this.addDiffStat(newRevision, diffStat);
			}
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
		
		private static String getExtension(String path) {
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

		@Override
		public void displayDeletedDirectory(String path, String rev1, String rev2) throws SVNException {
		}

		@Override
		public void displayAddedDirectory(String path, String rev1, String rev2) throws SVNException {
		}

		
	}
	
	static final class LongList {
		
		private int size;
		private long[] data;
		
		LongList() {
			this(16);
		}
		
		LongList(int initialSize) {
			this.size = 0;
			this.data = new long[initialSize];
		}
		
		void add(long l) {
			if (this.size == this.data.length) {
				long[] newData = new long[this.size * 2];
				System.arraycopy(this.data, 0, newData, 0, this.size);
				this.data = newData;
			}
			this.data[this.size++] = l;
		}
		
		int size() {
			return this.size;
		}
		
	}
	
	static final class CommitCoordinate {

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

}
