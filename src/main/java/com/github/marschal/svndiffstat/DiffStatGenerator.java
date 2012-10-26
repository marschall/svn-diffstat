package com.github.marschal.svndiffstat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNDiffGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;


public class DiffStatGenerator {

	public static void main(String[] args) throws SVNException {
		System.out.println("!!!current user directory must be working copy!!!");
		FSRepositoryFactory.setup();
		SVNClientManager clientManager = SVNClientManager.newInstance();
		
		SVNURL repoUrl = SVNURL.parseURIEncoded("file:///Users/marschall/svn/memoryfilesystem");
		boolean stopOnCopy = true;
		boolean discoverChangedPaths = false;
		SVNRevision startRevision = SVNRevision.create(1L);
		SVNRevision endRevision = SVNRevision.HEAD;
		String author = "marschall";
		File workingCopy = new File("/Users/marschall/Documents/workspaces/default/memoryfilesystem-workingcopy");
		File[] paths = new File[]{workingCopy};
		RevisionCollector logHandler = new RevisionCollector(author);
		long limit = Long.MAX_VALUE;
		clientManager.getLogClient().doLog(paths, startRevision, endRevision, stopOnCopy, discoverChangedPaths,
				limit, logHandler);
		List<CommitCoordinate> coordinates = logHandler.getCoordinates();
		for (CommitCoordinate coordinate : coordinates) {
			long revision = coordinate.getRevision();
			System.out.println(revision + ": " + coordinate.getDate());
		}
		System.out.println("=========================");
		
		SVNDiffClient diffClient = clientManager.getDiffClient();
//		Set<String> includedFiles = new HashSet<>(Arrays.asList("java", "xml"));
		Set<String> includedFiles = Collections.singleton("java");
		diffClient.setDiffGenerator(new DiffStatDiffGenerator(diffClient.getDiffGenerator(), includedFiles));
		SVNDepth depth = SVNDepth.INFINITY;
		boolean useAncestry = true;
//		diffClient.setGitDiffFormat(true);
		Collection<String> changeLists = null;
		for (CommitCoordinate coordinate : coordinates) {
			long revision = coordinate.getRevision();
			OutputStream result = System.out;
			SVNRevision newRevision = SVNRevision.create(revision);
			SVNRevision oldRevision = SVNRevision.create(revision - 1L);
			SVNRevision peg = null;
//			diffClient.doDiff
			diffClient.doDiff(workingCopy, oldRevision, workingCopy, newRevision, depth, useAncestry, result, changeLists);
		}
	}
	
	static final class RevisionCollector implements ISVNLogEntryHandler {
		
		private final String author;
		private final List<CommitCoordinate> coordinates;
		
		RevisionCollector(String author) {
			this.author = author;
			this.coordinates = new ArrayList<>();
		}


		@Override
		public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
			String logEntryAuthor = logEntry.getAuthor();
			if (this.author.equals(logEntryAuthor)) {
				long revision = logEntry.getRevision();
				Date date = logEntry.getDate();
				CommitCoordinate coordinate = new CommitCoordinate(revision, date);
				this.coordinates.add(coordinate);
			}
			
		}
		
		List<CommitCoordinate> getCoordinates() {
			return coordinates;
		}
		
	}
	
	static final class DiffStatDiffGenerator implements ISVNDiffGenerator {
		
		private final ISVNDiffGenerator delegate;
		private final List<DiffStat> diffStats;
		private final Set<String> includedFileExtensions;
		
		DiffStatDiffGenerator(ISVNDiffGenerator delegate, Set<String> includedFileExtensions) {
			this.delegate = delegate;
			this.includedFileExtensions = includedFileExtensions;
			this.diffStats = new ArrayList<>();
		}
		
		List<DiffStat> getDiffStats() {
			return this.diffStats;
		}
		
		void clearDiffStats() {
			this.diffStats.clear();
		}

		@Override
		public void init(String anchorPath1, String anchorPath2) {
			this.delegate.init(anchorPath1, anchorPath2);
		}

		@Override
		public void setBasePath(File basePath) {
			this.delegate.setBasePath(basePath);
		}

		@Override
		public void setForcedBinaryDiff(boolean forced) {
			this.delegate.setForcedBinaryDiff(forced);
		}

		@Override
		public boolean isForcedBinaryDiff() {
			return this.delegate.isForcedBinaryDiff();
		}

		@Override
		public void setEncoding(String encoding) {
			this.delegate.setEncoding(encoding);
		}

		@Override
		public String getEncoding() {
			return this.delegate.getEncoding();
		}

		@Override
		public void setEOL(byte[] eol) {
			this.delegate.setEOL(eol);
		}

		@Override
		public byte[] getEOL() {
			return this.delegate.getEOL();
		}

		@Override
		public void setDiffDeleted(boolean isDiffDeleted) {
			this.delegate.setDiffDeleted(isDiffDeleted);
		}

		@Override
		public boolean isDiffDeleted() {
			return this.delegate.isDiffDeleted();
		}

		@Override
		public void setDiffAdded(boolean isDiffAdded) {
			this.delegate.setDiffAdded(isDiffAdded);
		}

		@Override
		public boolean isDiffAdded() {
			return this.delegate.isDiffAdded();
		}

		@Override
		public void setDiffCopied(boolean isDiffCopied) {
			this.delegate.setDiffCopied(isDiffCopied);
		}

		@Override
		public boolean isDiffCopied() {
			return this.delegate.isDiffCopied();
		}

		@Override
		public void setDiffUnversioned(boolean diffUnversioned) {
			this.delegate.setDiffUnversioned(diffUnversioned);
			
		}

		@Override
		public boolean isDiffUnversioned() {
			return this.delegate.isDiffUnversioned();
		}

		@Override
		public File createTempDirectory() throws SVNException {
			return this.delegate.createTempDirectory();
		}

		@Override
		public void displayPropDiff(String path, SVNProperties baseProps, SVNProperties diff, OutputStream result) throws SVNException {
		}

		@Override
		public void displayFileDiff(String path, File file1, File file2,
				String rev1, String rev2, String mimeType1, String mimeType2,
				OutputStream result) throws SVNException {
			if (this.considerFile(path)) {
				ResetOutStream resetOutStream = new ResetOutStream();
				this.delegate.displayFileDiff(path, file1, file2, rev1, rev2, mimeType1, mimeType2, result);
				this.delegate.displayFileDiff(path, file1, file2, rev1, rev2, mimeType1, mimeType2, resetOutStream);
				DiffStat diffStat = resetOutStream.finish();
				System.out.printf("%n%s%n", diffStat);
			}
		}
		
		private boolean considerFile(String path) {
			if (path == null || path.isEmpty()) {
				return false;
			}
			String extension = getExtension(path);
			return extension != null && this.includedFileExtensions.contains(extension);
		}
		
		private static String getExtension(String path) {
			int lastIndex = lastIndexOf('.', path);
			if (lastIndex == -1 || lastIndex == path.length() - 1) {
				return null;
			}
			return path.substring(lastIndex + 1, path.length());
		}
		
		private static int lastIndexOf(char c, String s) {
			int lastIndex = s.indexOf(c);
			if (lastIndex == -1) {
				return lastIndex;
			}
			while (true) {
				int nextIndex = s.indexOf(c, lastIndex + 1);
				if (nextIndex == -1) {
					return lastIndex;
				}
				lastIndex = nextIndex;
			}
			
		}

		@Override
		public void displayDeletedDirectory(String path, String rev1, String rev2) throws SVNException {
		}

		@Override
		public void displayAddedDirectory(String path, String rev1, String rev2) throws SVNException {
		}

		
	}
	
	static final class LongList {
		
		private int size;
		private long[] data;
		
		LongList() {
			this(16);
		}
		
		LongList(int initialSize) {
			this.size = 0;
			this.data = new long[initialSize];
		}
		
		void add(long l) {
			if (this.size == this.data.length) {
				long[] newData = new long[this.size * 2];
				System.arraycopy(this.data, 0, newData, 0, this.size);
				this.data = newData;
			}
			this.data[this.size++] = l;
		}
		
		int size() {
			return this.size;
		}
		
	}
	
	static final class CommitCoordinate {

		private final long revision;
		private final Date date;

		CommitCoordinate(long revision, Date date) {
			this.revision = revision;
			this.date = date;
		}
		
		long getRevision() {
			return revision;
		}
		
		Date getDate() {
			return date;
		}
		
	}

}
