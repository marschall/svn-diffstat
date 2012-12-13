SVN DIFFSTAT
============
Creates git like diff stat over a whole svn repository. Only one author and a certain set of files are considered. In addition a chart similar to [Code Frequency](https://github.com/blog/1093-introducing-the-new-github-graphs) is created.

This is very raw and not polished software. These are the steps that you need to do to run the software
* build the software with <code>mvn clean package</code>
* check out the part of the subversion repository you're interested in
* run the software in the folder of the working copy you just checked out

Command Line Options
--------------------
The following command line options are supported:
<dl>
	<dt>-author -a</dt>
	<dd>Required. The authors that should be analyzed.</dd>
	<dt>-file -f</dt>
	<dd>Required. The file where to save the generated chart. Will always be a PNG.</dd>
	<dt>-extension -e</dt>
	<dd>Optional. The file extensions that should be analyzed.</dd>
	<dt>-width -w</dt>
	<dd>Optional. The width of the chart in pixels. Defaults to <code>1200</code>.</dd>
	<dt>-height -h</dt>
	<dd>Optional. The height of the chart in pixels. Defaults to <code>600</code>.</dd>
	<dt>-double -d -retina -r</dt>
	<dd>Optional. Render in double the resolution, useful for retina Macs. Defaults to <code>false</code>.</dd>
	<dt>-max -m</dt>
	<dd>Optional. Commits with more than this number of lines changed will be ignored. Defaults to <code>10000</code>.</dd>
</dl>

For example

    java -Djava.awt.headless=true -jar svn-diffstat/target/svn-diffstat-1.0.0-SNAPSHOT.jar -d -a marschall -e java -f /Users/marschall/tmp/diffstat.png

Sample
------
This is a sample chart generated from an actual code base.

<img src="https://raw.github.com/marschall/svn-diffstat/master/src/site/sample.png" width="800" height="400" alt="sample chart"/>
    
FAQ
---

### I'm getting NonWritableChannelException on file:// checkouts
```
Exception in thread "main" java.nio.channels.NonWritableChannelException
        at sun.nio.ch.FileChannelImpl.tryLock(FileChannelImpl.java:1014)
```
Just try again, somebody committed during the analysis.

### Does it scale?
I ran it on a subversion repository with about 240k revisions and it takes about 20 minutes.

### Is it any good?
Yes

Travis CI
---------
[![Build Status](https://secure.travis-ci.org/marschall/svn-diffstat.png?branch=master)](https://travis-ci.org/marschall/svn-diffstat)

Credits
-------
This project wouldn't have been possible without the work of others:
* [SVNKit](http://svnkit.com/), a Java Subversion implementation, used to extract all the information from Subversion, [TMate open source license](http://svnkit.com/license.html)
* [JFreeChart](http://www.jfree.org/jfreechart/), a Java chart library, used to build the code frequency char, [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl.html) (LGPL)
* [Joda-Time](http://joda-time.sourceforge.net), a Java date and time library, use for date calculations, [Apache 2.0 License](http://joda-time.sourceforge.net/license.html)
* [JCommander](http://jcommander.org), a Java library to parse command line parameters, [Apache 2.0 License](https://github.com/cbeust/jcommander/blob/master/license.txt)

Last but not least I'd like to thank [Netcetera](https://github.com/netceteragroup) for sponsoring the work on this. This was literally a one-weekend-hack as you can see from the commit logs.

