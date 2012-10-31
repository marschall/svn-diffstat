package com.github.marschal.svndiffstat;

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
    long toFormat = Math.abs(number);
    if (toFormat < 0) {
      // abs(Long.MIN_VALUE) = abs(Long.MIN_VALUE)
      toFormat = Long.MAX_VALUE;
    }
    if (toFormat < 1000L) {
      toAppendTo.append(toFormat);
    } else if (toFormat < 1000000L) {
      long thousands = toFormat / 1000L;
      toAppendTo.append(thousands);

      long reminder = toFormat - (thousands * 1000L);
      if (reminder != 0L && reminder >= 100L) {
        toAppendTo.append('.');
        toAppendTo.append(reminder / 100L);
      }
      toAppendTo.append('k');

    } else {
      long millions = toFormat / 1000000L;
      toAppendTo.append(millions);

      long reminder = toFormat - (millions * 1000000L);
      if (reminder != 0L && reminder >= 100000L) {
        toAppendTo.append('.');
        toAppendTo.append(reminder / 100000L);
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
