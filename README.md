### foraging

The foraging experiment is a common pool resource experiment where participants are placed in randomized groups and
interact with a spatially explicit virtual resource environment. It is built on the
[csidex](http://bitbucket.org/virtualcommons/csidex) experiment framework and has been used to conduct research studies
at [Arizona State University](http://www.asu.edu), [Indiana University](http://www.iu.edu), and the [University of Alaska-Anchorage](http://www.uaa.alaska.edu).

### features

* participants move and interact with a resource in a real-time 2-D grid environment. 
* Easily pluggable resource growth dynamics. The default is a parameterized density-dependent growth function but there
  are also "patchy" resource growth dynamics where the top half of the grid has a higher chance of regrowth than the
  bottom half. Arbitrary regrowth dynamics are possible by implementing a Java interface and specifying it in a
  configuration file.
* support for interactive quizzes, logged chat, message censoring, voting, and sanctioning
* binary and XML savefiles that store every user action in a time ordered stream that can be converted to QuickTime
  movies and replayed.

### how to run the software

Quickstart:

* [install and setup Java, Ant, and Maven](https://bitbucket.org/virtualcommons/csidex/wiki/Home)
* [download and unpack the foraging codebase](/virtualcommons/foraging/downloads)
* From the command-line (e.g., Windows PowerShell or Command Prompt, Mac OSX Terminal.app, or any Linux terminal program)
```
#!bash
% ant prepare-demo
% ant demo
```
For more detailed instructions, please see the [installation instructions on our wiki](wiki/Installation).

### publications

Data generated from the foraging framework has been published in 
[Lab Experiments for the Study of Social-Ecological Systems](http://www.sciencemag.org/cgi/content/abstract/328/5978/613). 
Archives of the [the configuration files used](src/main/resources/configuration/iu/archived) and the
[generated data](http://dev.commons.asu.edu/data/foraging/2008/all-iu-foraging-data.zip) are available.

### status
This project is not under active development but is actively maintained. If you'd like to add new features or experience
bugs using it, please let us know.
