package com.github.marschal.svndiffstat;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class ProtocolParameterValidator implements IValueValidator<String> {
  
  static final String FILE = "file";
  static final String DAV = "dav";
  static final String SVN = "svn";

  @Override
  public void validate(String name, String value) throws ParameterException {
    if (value != null) {
      if (!value.equals(FILE) && !value.equals(DAV) && !value.equals(SVN)) {
        throw new ParameterException("Parameter " + name + " should be missing or one of \"" + FILE + "\", \"" + DAV + "\", \"" + SVN + "\" (found " + value +")");
      }
    }

  }

}
