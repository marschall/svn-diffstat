package com.github.marschal.svndiffstat;

import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;


public class Main {

	public static void main(String[] args) {
		FSRepositoryFactory.setup();
		SVNClientManager clientManager = SVNClientManager.newInstance();
		
		clientManager.getLogClient();
		clientManager.getDiffClient();
	}

}
