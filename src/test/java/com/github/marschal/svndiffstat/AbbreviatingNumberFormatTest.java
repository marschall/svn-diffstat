package com.github.marschal.svndiffstat;

import java.text.NumberFormat;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbbreviatingNumberFormatTest {

  private NumberFormat format;

  @Before
  public void setUp() {
    this.format = new AbbreviatingNumberFormat();
  }

  @Test
  public void positiveSingleDigits() {
    assertEquals("0", this.format.format(0));
    assertEquals("1", this.format.format(1));
    assertEquals("10", this.format.format(10));
    assertEquals("100", this.format.format(100));
  }

  @Test
  public void negativeSingleDigits() {
    assertEquals("1", this.format.format(-1));
    assertEquals("10", this.format.format(-10));
    assertEquals("100", this.format.format(-100));
  }

  @Test
  public void positiveKs() {
    assertEquals("1k", this.format.format(1000));
    assertEquals("1.1k", this.format.format(1100));
    assertEquals("2k", this.format.format(2000));
    assertEquals("2.1k", this.format.format(2100));
    assertEquals("9.5k", this.format.format(9500));
  }

  @Test
  public void negativeks() {
    assertEquals("1k", this.format.format(-1000));
    assertEquals("1.1k", this.format.format(-1100));
    assertEquals("2k", this.format.format(-2000));
    assertEquals("2.1k", this.format.format(-2100));
    assertEquals("9.5k", this.format.format(-9500));
  }

  @Test
  public void positiveMs() {
    assertEquals("1m", this.format.format(1000000));
    assertEquals("1.1m", this.format.format(1100000));
    assertEquals("2m", this.format.format(2000000));
    assertEquals("2.1m", this.format.format(2100000));
    assertEquals("9.5m", this.format.format(9500000));
    assertEquals("11m", this.format.format(11000000));
  }

  @Test
  public void negativeMs() {
    assertEquals("1m", this.format.format(-1000000));
    assertEquals("1.1m", this.format.format(-1100000));
    assertEquals("2m", this.format.format(-2000000));
    assertEquals("2.1m", this.format.format(-2100000));
    assertEquals("9.5m", this.format.format(-9500000));
    assertEquals("11m", this.format.format(-11000000));
  }

}
