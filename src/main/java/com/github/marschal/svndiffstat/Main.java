package com.github.marschal.svndiffstat;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;


public class Main {

	public static void main(String[] args) throws SVNException {
		FSRepositoryFactory.setup();
		SVNClientManager clientManager = SVNClientManager.newInstance();
		
		SVNURL repoUrl = SVNURL.parseURIEncoded("file:///Users/marschall/svn/memoryfilesystem");
		boolean stopOnCopy = true;
		boolean discoverChangedPaths = false;
		SVNRevision startRevision = SVNRevision.create(1L);
		SVNRevision endRevision = SVNRevision.HEAD;
		String author = "marschall";
		File[] paths = new File[]{new File("/Users/marschall/Documents/workspaces/default/memoryfilesystem-workingcopy")};
		ISVNLogEntryHandler logHandler = new RevisionCollector(author);
		long limit = Long.MAX_VALUE;
		clientManager.getLogClient().doLog(paths, startRevision, endRevision, stopOnCopy, discoverChangedPaths,
				limit, logHandler);
//		clientManager.getDiffClient();
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
				System.out.println(revision + " : " + date);
				CommitCoordinate coordinate = new CommitCoordinate(revision, date);
				this.coordinates.add(coordinate);
			}
			
		}
		
	}
	
	static final class CommitCoordinate {

		private final long revision;
		private final Date date;

		CommitCoordinate(long revision, Date date) {
			this.revision = revision;
			this.date = date;
		}
		
	}

}
