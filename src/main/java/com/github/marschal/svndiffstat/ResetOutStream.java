package com.github.marschal.svndiffstat;

import java.io.IOException;
import java.io.OutputStream;

class ResetOutStream extends OutputStream {
	
	private byte[] data;
	private int position;
	
	private int added;
	private int removed;
	
	ResetOutStream() {
		this(0x1FFF);
	}
	
	ResetOutStream(int capacity) {
		this.data = new byte[capacity];
		this.position = 0;
		this.added = 0;
		this.removed = 0;
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
