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
import static org.joda.time.DateTimeConstants.MONDAY;

final class YearWeek extends TimeAxisKey implements Comparable<YearWeek> {

  private final short year;
  private final byte week;

  YearWeek(short year, byte week) {
    this.year = year;
    this.week = week;
  }

  static final class YearWeekFactory implements TimeAxisKeyFactory {

    @Override
    public YearWeek fromDate(Date date) {
      return YearWeek.fromDate(date);
    }
  }

  static YearWeek fromDate(Date date) {
    LocalDate localDate = new DateTime(date.getTime()).toLocalDate();
    return fromLocalDate(localDate);
  }

  static YearWeek fromLocalDate(LocalDate localDate) {
    return new YearWeek((short) localDate.getWeekyear(),
        (byte) localDate.getWeekOfWeekyear());
  }

  @Override
  RegularTimePeriod toPeriod() {
    return new Week(this.week, this.year);
  }

  LocalDate toLocalDate() {
    // TODO is this really correct for end of year / start year
    // split weeks?
    return new LocalDate()
      .withWeekyear(this.year)
      .withWeekOfWeekyear(this.week)
      .withDayOfWeek(MONDAY);
  }

  @Override
  int unitsBetween(TimeAxisKey key, DateTickUnitType type) {
    YearWeek other = (YearWeek) key;
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
  YearWeek previous() {
    LocalDate localDate = this.toLocalDate();
    int week = localDate.getWeekOfWeekyear();
    if (week == localDate.weekOfWeekyear().getMinimumValue()) {
      LocalDate previoursYear = localDate.withWeekyear(localDate.getWeekyear() - 1);
      return fromLocalDate(previoursYear.withWeekOfWeekyear(previoursYear.weekOfWeekyear().getMaximumValue()));
    } else {
      return fromLocalDate(localDate.withWeekOfWeekyear(week - 1));
    }
  }

  @Override
  YearWeek next() {
    LocalDate localDate = this.toLocalDate();
    int week = localDate.getWeekOfWeekyear();
    if (week == localDate.weekOfWeekyear().getMaximumValue()) {
      LocalDate nextYear = localDate.withWeekyear(localDate.getWeekyear() + 1);
      return fromLocalDate(nextYear.withWeekOfWeekyear(nextYear.weekOfWeekyear().getMinimumValue()));
    } else {
      return fromLocalDate(localDate.withWeekOfWeekyear(week + 1));
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof YearWeek)) {
      return false;
    }
    YearWeek other = (YearWeek) obj;
    return this.year == other.year
        && this.week == other.week;
  }

  @Override
  public int hashCode() {
    // this should be a perfect hash function (no collisions before modulo / shift / divide)
    return this.year << 8 | this.week;
  }

  @Override
  public int compareTo(YearWeek o) {
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
