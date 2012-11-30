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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jfree.chart.axis.DateTickUnitType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class YearMonthTest {

 private DateFormat dateFormat;
  
  @Before
  public void setUp() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  }
  
  @Test
  public void monthsBetween() throws ParseException {
    Date fromDate = this.dateFormat.parse("2011-10-01T16:10:12");
    Date toDate = this.dateFormat.parse("2012-10-01T16:10:12");
    YearMonth from = YearMonth.fromDate(fromDate);
    YearMonth to = YearMonth.fromDate(toDate);
    
    assertEquals(0, from.unitsBetween(from, DateTickUnitType.MONTH));
    assertEquals(12, from.unitsBetween(to, DateTickUnitType.MONTH));
    
    fromDate = this.dateFormat.parse("2011-09-01T16:10:12");
    toDate = this.dateFormat.parse("2012-10-01T16:10:12");
    from = YearMonth.fromDate(fromDate);
    to = YearMonth.fromDate(toDate);
    assertEquals(13, from.unitsBetween(to, DateTickUnitType.MONTH));
    
    fromDate = this.dateFormat.parse("2011-10-01T16:10:12");
    toDate = this.dateFormat.parse("2012-09-01T16:10:12");
    from = YearMonth.fromDate(fromDate);
    to = YearMonth.fromDate(toDate);
    assertEquals(11, from.unitsBetween(to, DateTickUnitType.MONTH));
  }

}
