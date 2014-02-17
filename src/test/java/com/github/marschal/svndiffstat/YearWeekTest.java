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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;


public class YearWeekTest {

 private DateFormat dateFormat;
  
  @Before
  public void setUp() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  }
  

  @Test
  public void nextSameMonths() throws ParseException {
    Date date = this.dateFormat.parse("2012-10-01T16:10:12");
    YearWeek yearWeek = YearWeek.fromDate(date);
    assertEquals(new LocalDate(2012, 10, 1), yearWeek.toLocalDate());
    assertEquals(new LocalDate(2012, 10, 8), yearWeek.next().toLocalDate());
  }

  @Test
  public void nextAcrossMonths() throws ParseException {
    Date date = this.dateFormat.parse("2012-09-17T16:10:12");
    YearWeek yearWeek = YearWeek.fromDate(date);
    assertEquals(new LocalDate(2012, 9, 17), yearWeek.toLocalDate());
    assertEquals(new LocalDate(2012, 9, 24), yearWeek.next().toLocalDate());
  }

  @Test
  public void nextAcrosYears() throws ParseException {
    Date date = this.dateFormat.parse("2011-12-26T16:10:12");
    YearWeek yearWeek = YearWeek.fromDate(date);
    assertEquals(new LocalDate(2011, 12, 26), yearWeek.toLocalDate());
    assertEquals(new LocalDate(2012, 01, 2), yearWeek.next().toLocalDate());
  }
  

  @Test
  public void previousAcrossMonths() throws ParseException {
    Date date = this.dateFormat.parse("2012-10-01T16:10:12");
    YearWeek yearWeek = YearWeek.fromDate(date);
    assertEquals(new LocalDate(2012, 10, 01), yearWeek.toLocalDate());
    assertEquals(new LocalDate(2012, 9, 24), yearWeek.previous().toLocalDate());
  }

  @Test
  public void previousAcrosYears() throws ParseException {
    Date date = this.dateFormat.parse("2012-01-02T16:10:12");
    YearWeek yearWeek = YearWeek.fromDate(date);
    assertEquals(new LocalDate(2012, 01, 02), yearWeek.toLocalDate());
    assertEquals(new LocalDate(2011, 12, 26), yearWeek.previous().toLocalDate());
  }

}
