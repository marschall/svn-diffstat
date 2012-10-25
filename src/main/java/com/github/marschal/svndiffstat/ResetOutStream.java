package com.github.marschal.svndiffstat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

class ResetOutStream extends OutputStream {
	
	private static final String INDEX_PREFIX = "Index: ";
	private static final String MARKER = "===================================================================";
	private static final String OLD_FILE = "--- ";
	private static final String NEW_FILE = "+++ ";
	private static final String ADDED = "+";
	private static final String REMOVED = "-";
	
	private byte[] data;
	private int position;
	
	private int added;
	private int removed;
	
	private byte[] eol;
	private byte[] indexMarker;
	private byte[] oldFileMarker;
	private byte[] newFileMarker;
	private byte[] addedMarker;
	private byte[] removedMarker;
	
	ResetOutStream() {
		this(0x1FFF);
	}
	
	ResetOutStream(int capacity) {
		this.data = new byte[capacity];
		this.position = 0;
		this.added = 0;
		this.removed = 0;
		// FIXME
		this.eol = System.getProperty("line.separator").getBytes();
		this.setEncoding(System.getProperty("file.encoding"));
	}
	
	void setEncoding(String encoding) {
		try {
			this.indexMarker = INDEX_PREFIX.getBytes(encoding);
			this.oldFileMarker = OLD_FILE.getBytes(encoding);
			this.newFileMarker = NEW_FILE.getBytes(encoding);
			this.addedMarker = ADDED.getBytes(encoding);
			this.removedMarker = REMOVED.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("encoding " + encoding + " not supported", e);
		}
	}
	
	private boolean isAt(byte[] b, int position) {
		for (int i = 0; i < b.length; i++) {
			if (this.data[position + i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	
	private int indexOf() {
		// FIXME
		throw new UnsupportedOperationException();
	}
	
	void initialize() {
		this.added = 0;
		this.removed = 0;
		this.position = 0;
	}

	@Override
	public void write(int b) throws IOException {
		this.data[this.position++] = (byte) b;
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		int len = b.length;
		System.arraycopy(b, 0, data, position, len);
		this.position += len;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		System.arraycopy(b, off, data, position, len);
		this.position += len;
	}
	
	private void ensureCapacity() {
		
	}
	
	private void parseToEnd() {
		
	}
	
	DiffStat finish() {
		this.parseToEnd();
		return new DiffStat(added, removed);
	}


}
