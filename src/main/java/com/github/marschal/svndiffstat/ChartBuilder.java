package com.github.marschal.svndiffstat;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.Color.WHITE;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

final class ChartBuilder {

  static final Color DATE_LABEL = new Color(0x33, 0x33, 0x33);
  static final Color VALUE_LABEL = new Color(0x88, 0x88, 0x88);
  static final Color TOTAL_LABEL = new Color(0x04, 0x7D, 0xDA);
  static final Color TOTAL_FILL = new Color(0x04, 0x7D, 0xDA, 127); // also label
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

  static JFreeChart createChart(NavigableMap<TimeAxisKey, DiffStat> aggregatedDiffstats, DiffStatConfiguration configuration) {

    boolean legend = false;
    boolean tooltips = false;
    boolean urls = false;
    Font helvetica = new Font("Helvetica", Font.PLAIN, 11 * configuration.multiplierInt());

    XYDatasetMinMax datasetMinMax = createDeltaDataset("Additions and Delections", aggregatedDiffstats);
    XYDataset dataset = datasetMinMax.dataset;
    JFreeChart chart = ChartFactory.createTimeSeriesChart("", "", "", dataset, legend, tooltips, urls);

    chart.setBackgroundPaint(WHITE);
    chart.setBorderVisible(false);

    float strokeWidth = 1.2f * configuration.multiplierFloat();

    XYPlot plot = chart.getXYPlot();
    plot.setOrientation(VERTICAL);
    plot.setBackgroundPaint(WHITE);
    plot.setDomainGridlinesVisible(true);
    plot.setDomainGridlinePaint(AXIS_LINE_COLOR);
    plot.setDomainGridlineStroke(new BasicStroke(1.0f * configuration.multiplierFloat()));
    plot.setRangeGridlinesVisible(false);

    plot.setOutlineVisible(false);

    DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
    dateAxis.setDateFormatOverride(new SimpleDateFormat("MM/yy"));
    dateAxis.setTickLabelFont(helvetica);
    dateAxis.setAxisLineVisible(false);
    dateAxis.setTickUnit(computeDateTickUnit(aggregatedDiffstats));
    RectangleInsets insets = new RectangleInsets(8.0d * configuration.multiplierDouble(),
        4.0d * configuration.multiplierDouble(),
        4.0d * configuration.multiplierDouble(),
        4.0d * configuration.multiplierDouble());
    dateAxis.setTickLabelInsets(insets);

    NumberAxis additionDeletionAxis = (NumberAxis) plot.getRangeAxis(0);
    additionDeletionAxis.setAxisLineVisible(false);
    additionDeletionAxis.setLabel("Additions and Deletions");
    additionDeletionAxis.setLabelFont(helvetica);
    additionDeletionAxis.setTickLabelFont(helvetica);
    additionDeletionAxis.setRangeType(RangeType.FULL);
    int lowerBound = datasetMinMax.min + (int) (datasetMinMax.min * 0.1d);
    additionDeletionAxis.setLowerBound(lowerBound);
    int upperBound = datasetMinMax.max + (int) (datasetMinMax.max * 0.1d);
    additionDeletionAxis.setUpperBound(upperBound);
    additionDeletionAxis.setNumberFormatOverride(new AbbreviatingNumberFormat());
    additionDeletionAxis.setMinorTickMarksVisible(false);
    additionDeletionAxis.setTickMarkInsideLength(5.0f * configuration.multiplierFloat());
    additionDeletionAxis.setTickMarkOutsideLength(0.0f);
    additionDeletionAxis.setTickMarkStroke(new BasicStroke(2.0f * configuration.multiplierFloat()));
    additionDeletionAxis.setTickUnit(new NumberTickUnit(computeTickUnitSize(datasetMinMax.max + abs(datasetMinMax.min))));

    XYAreaRenderer areaRenderer = new XYAreaRenderer(XYAreaRenderer.AREA);
    areaRenderer.setOutline(true);
    areaRenderer.setSeriesOutlinePaint(0, ADDED_STROKE);
    areaRenderer.setSeriesOutlineStroke(0, new BasicStroke(strokeWidth));
    areaRenderer.setSeriesPaint(0, ADDED_FILL);
    areaRenderer.setSeriesOutlinePaint(1, REMOVED_STROKE);
    areaRenderer.setSeriesOutlineStroke(1, new BasicStroke(strokeWidth));
    areaRenderer.setSeriesPaint(1, REMOVED_FILL);
    plot.setRenderer(0, areaRenderer);

    // Total Axis
    NumberAxis totalAxis = new NumberAxis("Total Lines");
    totalAxis.setAxisLineVisible(false);
    totalAxis.setLabelPaint(VALUE_LABEL);
    totalAxis.setTickLabelPaint(TOTAL_LABEL);
    totalAxis.setLabelFont(helvetica);
    totalAxis.setTickLabelFont(helvetica);
    totalAxis.setNumberFormatOverride(new AbbreviatingNumberFormat());
    totalAxis.setMinorTickMarksVisible(false);
    totalAxis.setTickMarkInsideLength(5.0f * configuration.multiplierFloat());
    totalAxis.setTickMarkOutsideLength(0.0f);
    totalAxis.setTickMarkStroke(new BasicStroke(2.0f * configuration.multiplierFloat()));
    totalAxis.setTickMarkPaint(TOTAL_LABEL);
    plot.setRangeAxis(1, totalAxis);
    plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

    XYDatasetMinMax datasetAndTotal = createTotalDataset("Total Lines", aggregatedDiffstats);
    XYDataset totalDataSet = datasetAndTotal.dataset;
    plot.setDataset(1, totalDataSet);
    plot.mapDatasetToRangeAxis(1, 1);
    // XYItemRenderer totalRenderer = new XYSplineRenderer();
    XYItemRenderer totalRenderer = new StandardXYItemRenderer();
    totalRenderer.setSeriesPaint(0, TOTAL_FILL);
    totalRenderer.setSeriesStroke(0, new BasicStroke(strokeWidth, CAP_ROUND, JOIN_ROUND,
        10.0f * configuration.multiplierFloat(),
        new float[]{6.0f * configuration.multiplierFloat(), 3.0f * configuration.multiplierFloat()} , 0.0f));
    plot.setRenderer(1, totalRenderer);

    totalAxis.setTickUnit(new NumberTickUnit(computeTickUnitSize(datasetAndTotal.max + abs(datasetAndTotal.min))));

    return chart;
  }

  static DateTickUnit computeDateTickUnit(NavigableMap<TimeAxisKey, DiffStat> aggregatedDiffstats) {
    TimeAxisKey start = aggregatedDiffstats.firstKey();
    TimeAxisKey end = aggregatedDiffstats.lastKey();

    int yearsBetween = start.unitsBetween(end, DateTickUnitType.YEAR);
    if (yearsBetween >= 5) {
      return new DateTickUnit(DateTickUnitType.YEAR, computeTickUnitSize(yearsBetween));
    }

    int monthsBetween = start.unitsBetween(end, DateTickUnitType.MONTH);
    if (monthsBetween >= 5) {
      return new DateTickUnit(DateTickUnitType.MONTH, computeTickUnitSize(monthsBetween));
    }

    // TODO check if day is supported
    int daysBetween = start.unitsBetween(end, DateTickUnitType.DAY);
    return new DateTickUnit(DateTickUnitType.DAY, computeTickUnitSize(daysBetween));
  }

  static int computeTickUnitSize(int maximum) {
    int tenbase = 1;
    while (tenbase * 10 < maximum) {
      tenbase *= 10;
    }
    int numberOfTicks = maximum / tenbase;
    if (numberOfTicks == 1) {
      return tenbase / 10;
    } else if (numberOfTicks <= 5 && tenbase > 10) {
      return tenbase / 2;
    } else {
      return tenbase;
    }
  }

  private static XYDatasetMinMax createDeltaDataset(String name, SortedMap<TimeAxisKey, DiffStat> aggregatedDiffstats) {

    TimeSeriesCollection dataset = new TimeSeriesCollection();
    int minimum = 0;
    int maximum = 0;

    TimeSeries addedSeries = new TimeSeries(name);
    TimeSeries removedSeries = new TimeSeries(name);
    for (Entry<TimeAxisKey, DiffStat> entry : aggregatedDiffstats.entrySet()) {
      TimeAxisKey timeAxisKey = entry.getKey();
      DiffStat diffStat = entry.getValue();
      RegularTimePeriod period = timeAxisKey.toPeriod();

      int added = diffStat.added();
      maximum = max(maximum, added);
      addedSeries.add(period, Integer.valueOf(added));

      int removed = -diffStat.removed();
      minimum = min(minimum, removed);
      removedSeries.add(period, Integer.valueOf(removed));
    }
    dataset.addSeries(addedSeries);
    dataset.addSeries(removedSeries);

    return new XYDatasetMinMax(dataset, minimum, maximum);
  }

  private static XYDatasetMinMax createTotalDataset(String name, SortedMap<TimeAxisKey, DiffStat> aggregatedDiffstats) {
    int total = 0;
    int maximum = Integer.MIN_VALUE;
    int minimum = 0;

    TimeSeries totalSeries = new TimeSeries(name);
    for (Entry<TimeAxisKey, DiffStat> entry : aggregatedDiffstats.entrySet()) {
      TimeAxisKey timeAxisKey = entry.getKey();
      DiffStat diffStat = entry.getValue();
      RegularTimePeriod period = timeAxisKey.toPeriod();
      total += diffStat.delta();
      maximum = max(total, maximum);
      minimum = min(total, minimum);
      totalSeries.add(period, Integer.valueOf(total));
    }

    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(totalSeries);

    return new XYDatasetMinMax(dataset, minimum, maximum);
  }

  static final class XYDatasetMinMax {
    final XYDataset dataset;
    final int min;
    final int max;

    XYDatasetMinMax(XYDataset dataset, int min, int max) {
      this.dataset = dataset;
      this.min = min;
      this.max = max;
    }

  }

}
