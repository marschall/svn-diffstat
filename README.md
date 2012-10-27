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
	<dt>-extension -e</dt>
	<dd>Required. The file extensions that should be analyzed.</dd>
	<dt>-file -f</dt>
	<dd>Required. The file where to save the generated chart. Will always be a PNG.</dd>
	<dt>-width -w</dt>
	<dd>Optional. The width of the chart in pixels. Defaults to <code>1200</code>.</dd>
	<dt>-height -h</dt>
	<dd>Optional. The height of the chart in pixels. Defaults to <code>600</code>.</dd>
	<dt>-double -d -retina -r</dt>
	<dd>Optional. Render in double the resolution, useful for retina Macs. Defaults to <code>false</code>.</dd>
</dl>

For example

    java -Djava.awt.headless=true -jar svn-diffstat/target/svn-diffstat-1.0.0-SNAPSHOT.jar -d -a marschall -e java -f /Users/marschall/tmp/diffstat.png
