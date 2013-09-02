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


public class YearMonthDayTest {
  
  private DateFormat dateFormat;
  
  @Before
  public void setUp() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  }

  @Test
  public void nextAcrossMonths() throws ParseException {
    Date date = this.dateFormat.parse("2012-09-30T16:10:12");
    YearMonthDay yearMonthDay = YearMonthDay.fromDate(date);
    assertEquals(new LocalDate(2012, 9, 30), yearMonthDay.toLocalDate());
    assertEquals(new LocalDate(2012, 10, 01), yearMonthDay.next().toLocalDate());
  }

  @Test
  public void nextAcrosYears() throws ParseException {
    Date date = this.dateFormat.parse("2011-12-31T16:10:12");
    YearMonthDay yearMonthDay = YearMonthDay.fromDate(date);
    assertEquals(new LocalDate(2011, 12, 31), yearMonthDay.toLocalDate());
    assertEquals(new LocalDate(2012, 01, 01), yearMonthDay.next().toLocalDate());
  }
  

  @Test
  public void previousAcrossMonths() throws ParseException {
    Date date = this.dateFormat.parse("2012-10-01T16:10:12");
    YearMonthDay yearMonthDay = YearMonthDay.fromDate(date);
    assertEquals(new LocalDate(2012, 10, 01), yearMonthDay.toLocalDate());
    assertEquals(new LocalDate(2012, 9, 30), yearMonthDay.previous().toLocalDate());
  }

  @Test
  public void previousAcrosYears() throws ParseException {
    Date date = this.dateFormat.parse("2012-01-01T16:10:12");
    YearMonthDay yearMonthDay = YearMonthDay.fromDate(date);
    assertEquals(new LocalDate(2012, 01, 01), yearMonthDay.toLocalDate());
    assertEquals(new LocalDate(2011, 12, 31), yearMonthDay.previous().toLocalDate());
  }

}
