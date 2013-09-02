package com.github.marschal.svndiffstat;

import static org.jfree.chart.axis.DateTickUnitType.DAY;
import static org.jfree.chart.axis.DateTickUnitType.MONTH;
import static org.jfree.chart.axis.DateTickUnitType.YEAR;

import java.util.Date;

import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;

final class YearMonthDay extends TimeAxisKey implements Comparable<YearMonthDay> {

  private final short year;
  private final byte month;
  private final byte day;

  YearMonthDay(short year, byte month, byte day) {
    this.year = year;
    this.month = month;
    this.day = day;
  }

  static final class YearMonthDayFactory implements TimeAxisKeyFactory {

    @Override
    public YearMonthDay fromDate(Date date) {
      return YearMonthDay.fromDate(date);
    }
  }

  static int daysBetween(Date first, Date second) {
    LocalDate firstLocalDate = fromDate(first).toLocalDate();
    LocalDate secondLocalDate = fromDate(second).toLocalDate();
    return Days.daysBetween(firstLocalDate, secondLocalDate).getDays();
  }

  static YearMonthDay fromDate(Date date) {
    LocalDate localDate = new DateTime(date.getTime()).toLocalDate();
    return fromLocalDate(localDate);
  }
  

  private static YearMonthDay fromLocalDate(LocalDate localDate) {
    return new YearMonthDay((short) localDate.getYear(),
        (byte) localDate.getMonthOfYear(),
        (byte) localDate.getDayOfMonth());
  }

  @Override
  RegularTimePeriod toPeriod() {
    return new Day(this.day, this.month, this.year);
  }

  LocalDate toLocalDate() {
    return new LocalDate(this.year, this.month, this.day);
  }

  @Override
  int unitsBetween(TimeAxisKey key, DateTickUnitType type) {
    YearMonthDay other = (YearMonthDay) key;
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
  YearMonthDay previous() {
    LocalDate localDate = this.toLocalDate();
    return fromLocalDate(localDate.minusDays(1));
  }

  @Override
  YearMonthDay next() {
    LocalDate localDate = this.toLocalDate();
    return fromLocalDate(localDate.plusDays(1));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof YearMonthDay)) {
      return false;
    }
    YearMonthDay other = (YearMonthDay) obj;
    return this.year == other.year
        && this.month == other.month
        && this.day == other.day;
  }

  @Override
  public int hashCode() {
    // this should be a perfect hash function (no collisions before modulo / shift / divide)
    return this.year << 16 | this.month << 8 | this.day;
  }

  @Override
  public int compareTo(YearMonthDay o) {
    int yearDiff = this.year - o.year;
    if (yearDiff != 0) {
      return yearDiff;
    }
    int monthDiff = this.month - o.month;
    if (monthDiff != 0) {
      return monthDiff;
    }
    return this.day - o.day;
  }

  @Override
  public String toString() {
    return "" + this.year + '-' + this.month + '-' + this.day;
  }

}
