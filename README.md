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

* [install Java, Ant, and Maven](https://bitbucket.org/virtualcommons/csidex/wiki/Home)
* download and unpack the foraging codebase
* `% cp build.properties.example build.properties`
* `% cp src/main/resources/configuration/demo/* src/main/resources/configuration`
* `% ant deploy`

For more detailed instructions, please see our [wiki](wiki/Home).

### publications

* [Lab Experiments for the Study of Social-Ecological Systems](http://www.sciencemag.org/cgi/content/abstract/328/5978/613)

### status
This project is not under active development but is actively maintained. If you'd like to add new features or experience
bugs using it, please let us know.
