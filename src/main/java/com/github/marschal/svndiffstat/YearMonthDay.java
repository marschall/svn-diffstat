package com.github.marschal.svndiffstat;

import java.util.Calendar;
import java.util.Date;

final class YearMonthDay implements Comparable<YearMonthDay> {
	
	private final int year;
	private final int month;
	private final int day;
	
	YearMonthDay(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	static YearMonthDay fromDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return fromCalendar(calendar);
	}

	private static YearMonthDay fromCalendar(Calendar calendar) {
		return new YearMonthDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}
	
	YearMonthDay previous() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, this.year);
		calendar.set(Calendar.MONTH, this.month);
		calendar.set(Calendar.DAY_OF_MONTH, this.day);
		calendar.roll(Calendar.DAY_OF_MONTH, false);
		return fromCalendar(calendar);
	}
	
	int year() {
		return this.year;
	}
	
	int month() {
		return this.month;
	}
	
	int day() {
		return this.day;
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
		int dayDiff = this.day - o.day;
		if (dayDiff != 0) {
			return dayDiff;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return "" + this.year + '-' + this.month + '-' + this.day;
	}

}
