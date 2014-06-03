package com.github.marschal.svndiffstat;

import static java.lang.Math.abs;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

final class AbbreviatingNumberFormat extends NumberFormat {

  @Override
  public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
    return this.format((long) number, toAppendTo, pos);
  }

  @Override
  public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
    long toFormat = abs(number);
    if (toFormat < 0) {
      // Java Puzzler:
      // abs(Long.MIN_VALUE) = Long.MIN_VALUE
      toFormat = Long.MAX_VALUE;
    }
    if (toFormat < 1_000L) {
      toAppendTo.append(toFormat);
    } else if (toFormat < 1_000_000L) {
      long thousands = toFormat / 1_000L;
      toAppendTo.append(thousands);

      long reminder = toFormat - (thousands * 1_000L);
      if (reminder != 0L && reminder >= 100L) {
        toAppendTo.append('.');
        toAppendTo.append(reminder / 100L);
      }
      toAppendTo.append('k');

    } else {
      long millions = toFormat / 1_000_000L;
      toAppendTo.append(millions);

      long remainder = toFormat - (millions * 1_000_000L);
      if (remainder != 0L && remainder >= 100_000L) {
        toAppendTo.append('.');
        toAppendTo.append(remainder / 100_000L);
      }
      toAppendTo.append('m');
    }
    return toAppendTo;
  }

  @Override
  public Number parse(String source, ParsePosition parsePosition) {
    throw new UnsupportedOperationException("parse not supported");
  }

}
