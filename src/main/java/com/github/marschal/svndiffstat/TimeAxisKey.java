package com.github.marschal.svndiffstat;

import java.util.Date;

import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.RegularTimePeriod;

abstract class TimeAxisKey {

  abstract RegularTimePeriod toPeriod();

  abstract int unitsBetween(TimeAxisKey other, DateTickUnitType type);

  interface TimeAxisKeyFactory {

    TimeAxisKey fromDate(Date date);

  }

  abstract TimeAxisKey previous();

  abstract TimeAxisKey next();

}
