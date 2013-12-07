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
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;


class YearMonthWrapper extends TimeAxisKey implements Comparable<YearMonthWrapper> {
  
  private final YearMonth yearMonth;

  YearMonthWrapper(YearMonth yearMonth) {
    this.yearMonth = yearMonth;
  }
  
  static final class YearMonthFactory implements TimeAxisKeyFactory {

    @Override
    public YearMonthWrapper fromDate(Date date) {
      return YearMonthWrapper.fromDate(date);
    }
  }

  static YearMonthWrapper fromDate(Date date) {
    Instant instant = Instant.ofEpochMilli(date.getTime());
    YearMonth yearMonth = YearMonth.from(instant);
    return new YearMonthWrapper(yearMonth);
  }

  @Override
  RegularTimePeriod toPeriod() {
    return new Month(this.yearMonth.getMonthValue(), this.yearMonth.getYear());
  }

  @Override
  int unitsBetween(TimeAxisKey key, DateTickUnitType type) {
    YearMonthWrapper other = (YearMonthWrapper) key;
    if (type == YEAR) {
      return other.yearMonth.getYear() - this.yearMonth.getYear();
    } else if (type == MONTH) {
      return (other.yearMonth.getYear() - this.yearMonth.getYear()) * 12
          + (other.yearMonth.getMonthValue() - this.yearMonth.getMonthValue());
    } else if (type == DAY) {
      Days daysBetween = Days.daysBetween(this.yearMonth, other.yearMonth);
      return daysBetween.getDays();
    } else {
      throw new IllegalArgumentException("unsupported tick type: " + type);
    }
  }

  @Override
  TimeAxisKey previous() {
    return new YearMonthWrapper(this.yearMonth.minusMonths(-1L));
  }

  @Override
  TimeAxisKey next() {
    return new YearMonthWrapper(this.yearMonth.plusMonths(-1L));
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof YearMonthWrapper)) {
      return false;
    }
    YearMonthWrapper other = (YearMonthWrapper) obj;
    return this.yearMonth.equals(other.yearMonth);
  }

  @Override
  public int hashCode() {
    // this should be a perfect hash function (no collisions before modulo / shift / divide)
    return this.yearMonth.hashCode();
  }

  @Override
  public int compareTo(YearMonthWrapper o) {
    return this.yearMonth.compareTo(o.yearMonth);
  }

  @Override
  public String toString() {
    return "" + this.yearMonth.getYear() + '-' + this.yearMonth.getMonthValue();
  }

}
