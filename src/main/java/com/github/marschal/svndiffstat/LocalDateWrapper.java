package com.github.marschal.svndiffstat;

import static org.jfree.chart.axis.DateTickUnitType.DAY;
import static org.jfree.chart.axis.DateTickUnitType.MONTH;
import static org.jfree.chart.axis.DateTickUnitType.YEAR;

import java.util.Date;

import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

final class LocalDateWrapper extends TimeAxisKey implements Comparable<LocalDateWrapper> {

  private final LocalDate localDate;

  LocalDateWrapper(LocalDate localDate) {
    this.localDate = localDate;
  }

  static final class YearMonthDayFactory implements TimeAxisKeyFactory {

    @Override
    public LocalDateWrapper fromDate(Date date) {
      return LocalDateWrapper.fromDate(date);
    }
  }

  static int daysBetween(Date first, Date second) {
    LocalDate firstLocalDate = fromDate(first).toLocalDate();
    LocalDate secondLocalDate = fromDate(second).toLocalDate();
    return Days.daysBetween(firstLocalDate, secondLocalDate).getDays();
  }

  static LocalDateWrapper fromDate(Date date) {
    Instant instant = Instant.ofEpochMilli(date.getTime());
    LocalDate localDate = LocalDate.from(instant);
    return new LocalDateWrapper(localDate);
  }
  

  private static LocalDateWrapper fromLocalDate(LocalDate localDate) {
    return new LocalDateWrapper(localDate);
  }

  @Override
  RegularTimePeriod toPeriod() {
    return new Day(this.localDate.getDayOfMonth(), this.localDate.getMonthValue(), this.localDate.getYear());
  }

  @Override
  int unitsBetween(TimeAxisKey key, DateTickUnitType type) {
    LocalDateWrapper other = (LocalDateWrapper) key;
    if (type == YEAR) {
      return other.year - this.year;
    } else if (type == MONTH) {
      Months monthsBetween = Months.monthsBetween(this.localDate, other.localDate);
      return monthsBetween.getMonths();
    } else if (type == DAY) {
      Days daysBetween = Days.daysBetween(this.localDate, other.localDate);
      return daysBetween.getDays();
    } else {
      throw new IllegalArgumentException("unsupported tick type: " + type);
    }
  }

  @Override
  LocalDateWrapper previous() {
    return fromLocalDate(localDate.minusDays(1));
  }

  @Override
  LocalDateWrapper next() {
    return fromLocalDate(localDate.plusDays(1));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LocalDateWrapper)) {
      return false;
    }
    LocalDateWrapper other = (LocalDateWrapper) obj;
    return this.localDate.equals(other.localDate);
  }

  @Override
  public int hashCode() {
    return this.localDate.hashCode();
  }

  @Override
  public int compareTo(LocalDateWrapper o) {
    return this.localDate.compareTo(o.localDate);
  }

  @Override
  public String toString() {
    return this.localDate.toString();
  }

}
