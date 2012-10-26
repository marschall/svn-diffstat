package com.github.marschal.svndiffstat;

import java.io.File;
import java.util.Set;

final class DiffStatConfiguration {
	
	private final String author;
	private final Set<String> includedFiles;
	private final File workingCopy;
	
	DiffStatConfiguration(String author, Set<String> includedFiles, File workingCopy) {
		this.author = author;
		this.includedFiles = includedFiles;
		this.workingCopy = workingCopy;
	}
	
	String getAuthor() {
		return this.author;
	}
	
	Set<String> getIncludedFiles() {
		return this.includedFiles;
	}
	
	File getWorkingCopy() {
		return this.workingCopy;
	}
	

}
