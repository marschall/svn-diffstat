package com.github.marschal.svndiffstat;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

import com.beust.jcommander.JCommander;

public class Main {

	public static void main(String[] args) throws IOException, SVNException {
		DiffStatConfiguration configuration = parse(args);
		init();
		ProgressReporter reporter = new ProgressReporter(System.out);
		NavigableMap<TimeAxisKey, DiffStat> aggregatedDiffStats = DiffStatGenerator.getData(configuration, reporter);
		
		saveAndDisplayChart(configuration, aggregatedDiffStats, reporter);
	}

	private static void saveAndDisplayChart(DiffStatConfiguration configuration, NavigableMap<TimeAxisKey, DiffStat> aggregatedDiffStats, ProgressReporter reporter) throws IOException {
		JFreeChart chart = ChartBuilder.createChart(aggregatedDiffStats, configuration);
		Path savePath = configuration.getSavePath();
		if (savePath != null) {
			Dimension dimension = configuration.getDimension();
			reporter.saveTo(savePath);
			ChartUtilities.saveChartAsPNG(savePath.toFile(), chart, dimension.width * configuration.multiplierInt(), dimension.height * configuration.multiplierInt());
			reporter.saveDone();
		} else {
			ChartBuilder.displayChard(chart, configuration);
		}
		
	}
	
	private static void init() {
		FSRepositoryFactory.setup();
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
	}

	private static DiffStatConfiguration parse(String[] args) {
		ConsoleConfiguration configuration = new ConsoleConfiguration();
		JCommander commander = new JCommander(configuration, args);

		File workingCopy = new File("").getAbsoluteFile();
		Dimension dimension = new Dimension(configuration.width, configuration.height);
		Set<String> authors = toSet(configuration.authors);
		Set<String> extensions = toSet(configuration.extensions);
		Path savePath = Paths.get(configuration.savePath);
		boolean doubleSize = configuration.doubleSize;
		return new DiffStatConfiguration(authors, extensions, workingCopy, dimension, savePath, doubleSize, configuration.max);
	}
	
	static <T> Set<T> toSet(List<T> list) {
		if (list.isEmpty()) {
			// a set containing everything
			return new FullSet<>();
		}
		if (list.size() == 1) {
			return Collections.singleton(list.get(0));
		} else {
			return new HashSet<>(list);
		}
		
	}

}
