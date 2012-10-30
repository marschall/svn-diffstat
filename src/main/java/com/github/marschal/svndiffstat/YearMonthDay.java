package com.github.marschal.svndiffstat;

import java.util.Calendar;
import java.util.Date;

import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import static org.jfree.chart.axis.DateTickUnitType.DAY;
import static org.jfree.chart.axis.DateTickUnitType.MONTH;
import static org.jfree.chart.axis.DateTickUnitType.YEAR;

final class YearMonthDay extends TimeAxisKey implements Comparable<YearMonthDay> {
	
	private final short year;
	private final byte month;
	private final byte day;
	
	private YearMonthDay(short year, byte month, byte day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	static final class YearMonthDayFactory implements TimeAxisKeyFactory {
		
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
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return fromCalendar(calendar);
	}

	private static YearMonthDay fromCalendar(Calendar calendar) {
		return new YearMonthDay((short) calendar.get(Calendar.YEAR),
				(byte) calendar.get(Calendar.MONTH),
				(byte) calendar.get(Calendar.DAY_OF_MONTH));
	}
	
	RegularTimePeriod toPeriod() {
		// compensate for the fact that java.util.Calendar months are 0-based
		// and org.jfree.data.time.Day months are 1-based
		return new Day(this.day, this.month + (1 - Calendar.JANUARY), this.year);
	}
	
	private LocalDate toLocalDate() {
		return new LocalDate(year, month, day);
	}
	
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
	
	YearMonthDay previous() {
		Calendar calendar = toCalendar();
		calendar.roll(Calendar.DAY_OF_MONTH, false);
		return fromCalendar(calendar);
	}

	private Calendar toCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, this.year);
		calendar.set(Calendar.MONTH, this.month);
		calendar.set(Calendar.DAY_OF_MONTH, this.day);
		return calendar;
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
