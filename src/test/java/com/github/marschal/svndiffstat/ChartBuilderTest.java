package com.github.marschal.svndiffstat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChartBuilderTest {

  @Test
  public void computeTickUnitSize() {
    assertEquals(1, ChartBuilder.computeTickUnitSize(4));
    assertEquals(1, ChartBuilder.computeTickUnitSize(10));
    
    assertEquals(500, ChartBuilder.computeTickUnitSize(4000));
    assertEquals(500, ChartBuilder.computeTickUnitSize(4500));
    assertEquals(500, ChartBuilder.computeTickUnitSize(5000));

    assertEquals(1000, ChartBuilder.computeTickUnitSize(8000));
    assertEquals(1000, ChartBuilder.computeTickUnitSize(8500));
    assertEquals(1000, ChartBuilder.computeTickUnitSize(9500));
    assertEquals(1000, ChartBuilder.computeTickUnitSize(10000));
    assertEquals(1000, ChartBuilder.computeTickUnitSize(11463));
  }

}
