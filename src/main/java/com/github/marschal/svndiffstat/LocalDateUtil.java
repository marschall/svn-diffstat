package com.github.marschal.svndiffstat;

import java.util.Date;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

final class LocalDateUtil {

  private LocalDateUtil() {
    throw new AssertionError("not instantiable");
  }
  

  static LocalDate fromDate(Date date) {
    Instant instant = Instant.ofEpochMilli(date.getTime());
    return LocalDate.from(instant);
  }

}
