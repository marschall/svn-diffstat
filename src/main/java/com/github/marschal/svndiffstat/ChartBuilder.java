package com.github.marschal.svndiffstat;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.Color.WHITE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

final class ChartBuilder {
	
	static final Color DATE_LABEL = new Color(0x33, 0x33, 0x33);
	static final Color VALUE_LABEL = new Color(0x88, 0x88, 0x88);
	static final Color DARK_BLUE = new Color(0x04, 0x7D, 0xDA);
	static final Color LIGHT_BLUE = new Color(0x04, 0x7D, 0xDA, 127); // also label
	static final Color DARK_GREY = new Color(0x33, 0x33, 0x33);
	static final Color ADDED_FILL = new Color(0x1D, 0xB3, 0x4F, 127); // also label
	static final Color ADDED_STROKE = new Color(0x1D, 0xB3, 0x4F);
	static final Color REMOVED_FILL = new Color(0xAD, 0x10, 0x17, 127);  // also label
	static final Color REMOVED_STROKE = new Color(0xAD, 0x10, 0x17);
	static final Color AXIS_LINE_COLOR = new Color(0xEE, 0xEE, 0xEE);
	
	
	static void displayChard(final JFreeChart chart, final DiffStatConfiguration configuration) {
        
        SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				ChartPanel chartPanel = new ChartPanel(chart);
				chartPanel.setPreferredSize(configuration.getDimension());
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
	
	static JFreeChart createChart(SortedMap<YearMonthDay, DiffStat> aggregatedDiffstats, DiffStatConfiguration configuration) {
		
		boolean legend = false;
        boolean tooltips = false;
        boolean urls = false;
        Font helvetica = new Font("Helvetica", Font.PLAIN, 11 * configuration.multiplierInt());
		
		XYDataset dataset = createDeltaDataset("Additions and Delections", aggregatedDiffstats);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("", "", "", dataset, legend, tooltips, urls);
		
		chart.setBackgroundPaint(WHITE);
		chart.setBorderVisible(false);
		
		float strokeWidth = 1.5f * configuration.multiplierFloat();
		
		XYPlot plot = chart.getXYPlot();
        plot.setOrientation(VERTICAL);
        plot.setBackgroundPaint(WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(AXIS_LINE_COLOR);
        plot.setDomainGridlineStroke(new BasicStroke(1.0f * configuration.multiplierFloat()));
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
        areaRenderer.setSeriesOutlineStroke(0, new BasicStroke(strokeWidth));
        areaRenderer.setSeriesPaint(0, ADDED_FILL);
        areaRenderer.setSeriesOutlinePaint(1, REMOVED_STROKE);
        areaRenderer.setSeriesOutlineStroke(1, new BasicStroke(strokeWidth));
        areaRenderer.setSeriesPaint(1, REMOVED_FILL);
		plot.setRenderer(0, areaRenderer);
		
		// Total Axix 2
        NumberAxis totalAxis = new NumberAxis("Total Lines");
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
        totalRenderer.setSeriesStroke(0, new BasicStroke(strokeWidth, CAP_ROUND, JOIN_ROUND,
        		10.0f * configuration.multiplierFloat(), new float[]{6.5f * configuration.multiplierFloat()} , 0.0f));
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

}
