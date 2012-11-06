package com.github.marschal.svndiffstat;

import java.util.Date;

import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Week;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import static org.jfree.chart.axis.DateTickUnitType.DAY;
import static org.jfree.chart.axis.DateTickUnitType.MONTH;
import static org.jfree.chart.axis.DateTickUnitType.YEAR;

final class YearMonth extends TimeAxisKey implements Comparable<YearMonth> {

  private final short year;
  private final byte week;

  YearMonth(short year, byte week) {
    this.year = year;
    this.week = week;
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
    return new YearMonth((short) localDate.getWeekyear(),
        (byte) localDate.getWeekOfWeekyear());
  }

  @Override
  RegularTimePeriod toPeriod() {
    return new Week(this.week, this.year);
  }

  LocalDate toLocalDate() {
    // TODO not sure if the is correct for end / star of year weeks
    return new LocalDate(this.year, 1, 1).plusWeeks(this.week - 1);
  }

  @Override
  int unitsBetween(TimeAxisKey key, DateTickUnitType type) {
    YearMonth other = (YearMonth) key;
    if (type == YEAR) {
      return other.year - this.year;
    } else if (type == MONTH) {
      Months monthsBetween = Months.monthsBetween(this.toLocalDate(), other.toLocalDate());
      return monthsBetween.getMonths();
    } else if (type == DAY) {
      Days daysBetween = Days.daysBetween(this.toLocalDate(), other.toLocalDate());
      return daysBetween.getDays();
    } else {
      throw new IllegalArgumentException("unsupported tick type: " + type);
    }
  }

  @Override
  YearMonth previous() {
    LocalDate localDate = this.toLocalDate();
    return fromLocalDate(localDate.minusMonths(1));
  }

  @Override
  YearMonth next() {
    LocalDate localDate = this.toLocalDate();
    return fromLocalDate(localDate.plusMonths(1));
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
        && this.week == other.week;
  }

  @Override
  public int hashCode() {
    // this should be a perfect hash function (no collisions before modulo / shift / divide)
    return this.year << 8 | this.week;
  }

  @Override
  public int compareTo(YearMonth o) {
    int yearDiff = this.year - o.year;
    if (yearDiff != 0) {
      return yearDiff;
    }
    return this.week - o.week;
  }

  @Override
  public String toString() {
    return "" + this.year + '-' + this.week;
  }

}
