/*
 * Copyright (C) 2012 by Netcetera AG.
 * All rights reserved.
 *
 * The copyright to the computer program(s) herein is the property of Netcetera AG, Switzerland.
 * The program(s) may be used and/or copied only with the written permission of Netcetera AG or
 * in accordance with the terms and conditions stipulated in the agreement/contract under which 
 * the program(s) have been supplied.
 */
package com.github.marschal.svndiffstat;

import static org.junit.Assert.assertEquals;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Test;
import org.threeten.bp.LocalDate;


public class DiffStatGeneratorTest {
  
  @Test
  public void insertZeroDataPointsNothingBetween() {
    NavigableMap<TimeAxisKey, DiffStat> diffStats = new TreeMap<>();
    LocalDate firstKey = LocalDate.of(2012, 1, 2);
    LocalDate secondKey = LocalDate.of(2012, 1, 3);
    
    diffStats.put(firstKey, new DiffStat(1, 1));
    diffStats.put(secondKey, new DiffStat(2, 2));
    // TODO fix hamcrest
    assertEquals(2, diffStats.size());
    
    DiffStatGenerator.insertZeroDataPoints(diffStats);
    assertEquals(2, diffStats.size());
    assertEquals(new DiffStat(1, 1), diffStats.get(firstKey));
    assertEquals(new DiffStat(2, 2), diffStats.get(secondKey));
  }

  @Test
  public void insertZeroDataPointsOneBetween() {
    NavigableMap<TimeAxisKey, DiffStat> diffStats = new TreeMap<>();
    YearMonthDay firstKey = new YearMonthDay((short) 2012, (byte) 1, (byte) 2);
    YearMonthDay secondKey = new YearMonthDay((short) 2012, (byte) 1, (byte) 4);
    diffStats.put(firstKey, new DiffStat(1, 1));
    diffStats.put(secondKey, new DiffStat(2, 2));

    assertEquals(2, diffStats.size());
    
    DiffStatGenerator.insertZeroDataPoints(diffStats);
    assertEquals(3, diffStats.size());
    assertEquals(new DiffStat(1, 1), diffStats.get(firstKey));
    assertEquals(new DiffStat(2, 2), diffStats.get(secondKey));
    // inserted
    assertEquals(new DiffStat(0, 0), diffStats.get(new YearMonthDay((short) 2012, (byte) 1, (byte) 3)));
  }
  

  @Test
  public void insertZeroDataPointsTwoBetween() {
    NavigableMap<TimeAxisKey, DiffStat> diffStats = new TreeMap<>();
    YearMonthDay firstKey = new YearMonthDay((short) 2012, (byte) 1, (byte) 2);
    YearMonthDay secondKey = new YearMonthDay((short) 2012, (byte) 1, (byte) 5);
    diffStats.put(firstKey, new DiffStat(1, 1));
    diffStats.put(secondKey, new DiffStat(2, 2));

    assertEquals(2, diffStats.size());
    
    DiffStatGenerator.insertZeroDataPoints(diffStats);
    assertEquals(4, diffStats.size());
    assertEquals(new DiffStat(1, 1), diffStats.get(firstKey));
    assertEquals(new DiffStat(2, 2), diffStats.get(secondKey));
    // inserted
    assertEquals(new DiffStat(0, 0), diffStats.get(new YearMonthDay((short) 2012, (byte) 1, (byte) 3)));
    assertEquals(new DiffStat(0, 0), diffStats.get(new YearMonthDay((short) 2012, (byte) 1, (byte) 4)));
  }
  

  @Test
  public void insertZeroDataPointsSeveralBetween() {
    NavigableMap<TimeAxisKey, DiffStat> diffStats = new TreeMap<>();
    YearMonthDay firstKey = new YearMonthDay((short) 2012, (byte) 1, (byte) 2);
    YearMonthDay secondKey = new YearMonthDay((short) 2012, (byte) 1, (byte) 10);
    diffStats.put(firstKey, new DiffStat(1, 1));
    diffStats.put(secondKey, new DiffStat(2, 2));

    assertEquals(2, diffStats.size());
    
    DiffStatGenerator.insertZeroDataPoints(diffStats);
    assertEquals(4, diffStats.size());
    assertEquals(new DiffStat(1, 1), diffStats.get(firstKey));
    assertEquals(new DiffStat(2, 2), diffStats.get(secondKey));
    // inserted
    assertEquals(new DiffStat(0, 0), diffStats.get(new YearMonthDay((short) 2012, (byte) 1, (byte) 3)));
    assertEquals(new DiffStat(0, 0), diffStats.get(new YearMonthDay((short) 2012, (byte) 1, (byte) 9)));
  }

}
