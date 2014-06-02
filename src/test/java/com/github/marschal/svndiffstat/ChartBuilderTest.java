package com.github.marschal.svndiffstat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChartBuilderTest {

  @Test
  public void computeTickUnitSize() {
    TickConfiguration configuration = new TickConfiguration(5, 10);
    assertEquals(1, ChartBuilder.computeTickUnitSize(4, configuration));
    assertEquals(1, ChartBuilder.computeTickUnitSize(10, configuration));
    
    assertEquals(500, ChartBuilder.computeTickUnitSize(4000, configuration));
    assertEquals(500, ChartBuilder.computeTickUnitSize(4500, configuration));
    assertEquals(500, ChartBuilder.computeTickUnitSize(5000, configuration));

    assertEquals(1000, ChartBuilder.computeTickUnitSize(8000, configuration));
    assertEquals(1000, ChartBuilder.computeTickUnitSize(8500, configuration));
    assertEquals(1000, ChartBuilder.computeTickUnitSize(9500, configuration));
    assertEquals(1000, ChartBuilder.computeTickUnitSize(10000, configuration));
    assertEquals(1000, ChartBuilder.computeTickUnitSize(11463, configuration));
  }

}
