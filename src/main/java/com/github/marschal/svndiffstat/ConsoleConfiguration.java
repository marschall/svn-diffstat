package com.github.marschal.svndiffstat;

import java.util.List;

import com.beust.jcommander.Parameter;

public class ConsoleConfiguration {

  @Parameter(names = {"-double", "-d", "-retina", "-r"},
      description = "Render in double the resolution, useful for retina Macs")
  boolean doubleSize = false;

  @Parameter(names = {"-width", "-w"},
      description = "The width of the chart in pixels")
  int width = 1200;

  @Parameter(names = {"-height", "-h"},
      description = "The height of the chart in pixels")
  int height = 600;

  @Parameter(names = {"-max", "-m"},
      description = "Commits with more than this number of lines changed will be ignored.")
  int max = 10000;

  @Parameter(names = {"-author", "-a"},
      description = "The authors that should be analyzed",
      required = true)
  List<String> authors;

  @Parameter(names = {"-extension", "-e"},
      description = "The file extensions that should be analized")
  List<String> extensions;

  @Parameter(names = {"-file", "-f"},
      description = "The file where to save the generated chart",
      required = true)
  String savePath;

  @Parameter(names = {"-protocol", "-p"},
      description = "Only initialize support for the given protocol. Options: fs dav svn",
      validateValueWith = ProtocolParameterValidator.class)
  String protocol;

  @Parameter(names = {"-tdal"},
      description = "The lowest number of date axis ticks to achieve.")
  int dateAxisTickLower = 5;

  @Parameter(names = {"-tdau"},
      description = "The highest number of date axis ticks to achieve.")
  int dateAxisTickUpper = 10;

  @Parameter(names = {"-tval"},
      description = "The lowest number of date axis ticks to achieve.")
  int valueAxisTickLower = 5;

  @Parameter(names = {"-tvau"},
      description = "The highest number of date axis ticks to achieve.")
  int valueAxisTickUpper = 10;
  
  @Parameter(names = {"-td"},
      description = "The highest number of domain ticks to achieve.")
  int domainTicks = 100;


}
