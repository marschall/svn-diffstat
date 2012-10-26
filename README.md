SVN DIFFSTAT
============
Creates git like diff stat over a whole svn repository. Only one author and a certain set of files are considered. In addition a chart similar to [Code Frequency](https://github.com/blog/1093-introducing-the-new-github-graphs) is created.

This is very raw and not polished software. These are the steps that you need to do to run the software
* check out the part of the subversion repository you're interested in
* update the configuration in <code>com.github.marschal.svndiffstat.DiffStatGenerator</code>
* run the software in the folder of the working copy you just checked out
