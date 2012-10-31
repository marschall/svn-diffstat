package com.github.marschal.svndiffstat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

class ResetOutputStream extends OutputStream {

  private static final String INDEX_PREFIX = "Index: ";
  private static final String MARKER = "===================================================================";
  private static final String OLD_FILE = "--- ";
  private static final String NEW_FILE = "+++ ";
  private static final String ADDED = "+";
  private static final String REMOVED = "-";

  private final byte[] data;
  private int writePosition;
  private int readPosition;

  private int added;
  private int removed;

  private byte[] eol;
  private byte[] indexMarker;
  private byte[] marker;
  private byte[] oldFileMarker;
  private byte[] newFileMarker;
  private byte[] addedMarker;
  private byte[] removedMarker;
  private boolean headerParsed;
  private String encoding;

  ResetOutputStream() {
    this(0x4000);
  }

  ResetOutputStream(int capacity) {
    this.data = new byte[capacity];
    this.writePosition = 0;
    this.readPosition = 0;
    this.added = 0;
    this.removed = 0;
    this.headerParsed = false;
    this.eol = System.getProperty("line.separator").getBytes();
    this.setEncoding(System.getProperty("file.encoding"));
  }


  void initialize() {
    this.added = 0;
    this.removed = 0;
    this.writePosition = 0;
    this.readPosition = 0;
    this.headerParsed = false;
  }



  void setEOL(byte[] eol) {
    this.eol = eol;
  }

  void setEncoding(String encoding) {
    this.encoding = encoding;
    try {
      this.indexMarker = INDEX_PREFIX.getBytes(encoding);
      this.marker = MARKER.getBytes(encoding);
      this.oldFileMarker = OLD_FILE.getBytes(encoding);
      this.newFileMarker = NEW_FILE.getBytes(encoding);
      this.addedMarker = ADDED.getBytes(encoding);
      this.removedMarker = REMOVED.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("encoding " + encoding + " not supported", e);
    }
  }

  private boolean startsWith(byte[] b) {
    int prefixLength = b.length;
    for (int i = 0; i < prefixLength; i++) {
      if (this.data[this.readPosition + i] != b[i]) {
        return false;
      }
    }
    return true;
  }

  private int indexOf(byte[] b) {
    int argumentLength = b.length;
    outerloop : for (int i = this.readPosition; i < this.writePosition; i++) {
      for (int j = 0; j < argumentLength; j++) {
        if (this.data[i + j] != b[j]) {
          continue outerloop;
        }
      }
      return i;
    }
    return -1;
  }


  @Override
  public void write(int b) throws IOException {
    this.ensureCapacity(1);
    this.data[this.writePosition++] = (byte) b;
  }

  @Override
  public void write(byte[] b) throws IOException {
    int len = b.length;
    this.ensureCapacity(len);
    System.arraycopy(b, 0, this.data, this.writePosition, len);
    this.writePosition += len;
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    this.ensureCapacity(len);
    System.arraycopy(b, off, this.data, this.writePosition, len);
    this.writePosition += len;
  }

  private void ensureCapacity(int capacity) {
    if (this.writePosition + capacity > this.data.length) {
      this.parse();

      int length = this.writePosition - this.readPosition;
      System.arraycopy(this.data, this.readPosition, this.data, 0, length);
      this.readPosition = 0;
      this.writePosition = length;
    }
  }

  private void parse() {
    if (!this.headerParsed) {
      // Index: path/to/file.extension
      this.expectMarker(this.indexMarker);
      // EOL
      this.consumeEol();
      // ===================================================================
      this.expectMarker(this.marker);
      // EOL
      this.consumeEol();
      // --- path/to/file.extension (revision x -1)
      this.expectMarker(this.oldFileMarker);
      // EOL
      this.consumeEol();
      // +++ path/to/file.extension (revision x)
      this.expectMarker(this.newFileMarker);
      // EOL
      this.consumeEol();
      this.headerParsed = true;
    }
    int eolIndex = this.indexOf(this.eol);
    while (eolIndex != -1) {
      if (this.startsWith(this.addedMarker)) {
        this.added += 1;
      } else if (this.startsWith(this.removedMarker)) {
        this.removed += 1;
      }
      this.readPosition = eolIndex + this.eol.length;
      eolIndex = this.indexOf(this.eol);
    }
  }

  private void expectMarker(byte[] marker) throws IllegalAccessError {
    boolean startsWithHeader = this.startsWith(marker);
    if (!startsWithHeader) {
      try {
        throw new IllegalArgumentException(new String(marker, this.encoding) + "expected");
      } catch (UnsupportedEncodingException e) {
        // this can't happen because we could previously decode strings
        // with this this encoding
        throw new AssertionError("encoding " + this.encoding + " not supported");
      }
    }
    this.readPosition += marker.length;
  }

  private void consumeEol() {
    int eolIndex = this.indexOf(this.eol);
    if (eolIndex == -1) {
      throw new IllegalArgumentException("EOL expected");
    }
    this.readPosition = eolIndex + this.eol.length;
  }

  DiffStat finish() {
    this.parse();
    return new DiffStat(this.added, this.removed);
  }


}
