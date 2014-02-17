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

import static org.jfree.chart.axis.DateTickUnitType.DAY;
import static org.jfree.chart.axis.DateTickUnitType.MONTH;
import static org.jfree.chart.axis.DateTickUnitType.YEAR;

import java.util.Date;

import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePartial;


class YearMonth extends TimeAxisKey implements Comparable<YearMonth> {
  
  private final short year;
  private final byte month;

  YearMonth(short year, byte month) {
    this.year = year;
    this.month = month;
  }
  
  static final class YearMonthFactory implements TimeAxisKeyFactory {

    @Override
    public YearMonth fromDate(Date date) {
      return YearMonth.fromDate(date);
    }
  }

  static YearMonth fromDate(Date date) {
    LocalDate localDate = new DateTime(date.getTime()).toLocalDate();
    return fromLocalDate(localDate);
  }
  
  static YearMonth fromLocalDate(LocalDate localDate) {
    return new YearMonth((short) localDate.getYear(),
        (byte) localDate.getMonthOfYear());
  }

  @Override
  RegularTimePeriod toPeriod() {
    return new Month(this.month, this.year);
  }

  ReadablePartial toPartial() {
    return new org.joda.time.YearMonth(this.year, this.month);
  }

  @Override
  int unitsBetween(TimeAxisKey key, DateTickUnitType type) {
    YearMonth other = (YearMonth) key;
    if (type == YEAR) {
      return other.year - this.year;
    } else if (type == MONTH) {
      return (other.year - this.year) * 12
          + (other.month - this.month);
    } else if (type == DAY) {
      Days daysBetween = Days.daysBetween(this.toPartial(), other.toPartial());
      return daysBetween.getDays();
    } else {
      throw new IllegalArgumentException("unsupported tick type: " + type);
    }
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
