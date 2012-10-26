package com.github.marschal.svndiffstat;

import java.awt.Dimension;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;

final class DiffStatConfiguration {
	
	private final String author;
	private final Set<String> includedFiles;
	private final File workingCopy;
	private final Dimension dimension;
	private Path savePath;
	
	DiffStatConfiguration(String author, Set<String> includedFiles, File workingCopy, Dimension dimension, Path savePath) {
		this.author = author;
		this.includedFiles = includedFiles;
		this.workingCopy = workingCopy;
		this.dimension = dimension;
		this.savePath = savePath;
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
	
	Dimension getDimension() {
		return this.dimension;
	}
	
	Path getSavePath() {
		return this.savePath;
	}
	

}
