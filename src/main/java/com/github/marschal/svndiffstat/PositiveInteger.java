package com.github.marschal.svndiffstat;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class PositiveInteger implements IValueValidator<Integer> {

  @Override
  public void validate(String name, Integer value) throws ParameterException {
    if (value <= 0) {
      throw new ParameterException("Parameter " + name + " should be positive (found " + value +")");
    }

  }

}
