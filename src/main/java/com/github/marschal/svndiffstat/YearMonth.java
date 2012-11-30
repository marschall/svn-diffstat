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

import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;


public class YearMonth extends TimeAxisKey implements Comparable<YearMonth> {
  
  private final short year;
  private final byte month;

  YearMonth(short year, byte month) {
    this.year = year;
    this.month = month;
  }

  @Override
  RegularTimePeriod toPeriod() {
    return new Month(this.month, this.year);
  }

  @Override
  int unitsBetween(TimeAxisKey other, DateTickUnitType type) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  TimeAxisKey previous() {
    if (this.month == 1) {
      return new YearMonth((short) (this.year - 1), (byte) 12);
    } else {
      return new YearMonth(this.year, (byte) (this.month - 1));
    }
  }

  @Override
  TimeAxisKey next() {
    if (this.month == 12) {
      return new YearMonth((short) (this.year + 1), (byte) 1);
    } else {
      return new YearMonth(this.year, (byte) (this.month + 1));
    }
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof YearMonth)) {
      return false;
    }
    YearMonth other = (YearMonth) obj;
    return this.year == other.year
        && this.month == other.month;
  }

  @Override
  public int hashCode() {
    // this should be a perfect hash function (no collisions before modulo / shift / divide)
    return this.year << 8 | this.month;
  }

  @Override
  public int compareTo(YearMonth o) {
    int yearDiff = this.year - o.year;
    if (yearDiff != 0) {
      return yearDiff;
    }
    return this.month - o.month;
  }

  @Override
  public String toString() {
    return "" + this.year + '-' + this.month;
  }

}
