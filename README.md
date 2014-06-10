### foraging

The foraging experiment is a common pool resource experiment where participants are placed in randomized groups and
interact with a spatially explicit renewable resource. It is built on the
[Social Ecological Systems Experiment Framework](http://bitbucket.org/virtualcommons/sesef) and has been used to conduct research studies
at [Arizona State University](http://www.asu.edu), [Indiana University](http://www.iu.edu), and the [University of Alaska-Anchorage](http://www.uaa.alaska.edu).

### features

* Participants move and interact with a resource in a real-time 2-D grid environment. 
* Pluggable resource growth dynamics. The default is a parameterized density-dependent growth function but there
  are also "patchy" resource growth dynamics where the top half of the grid has a higher chance of regrowth than the
  bottom half. Arbitrary regrowth dynamics are possible by implementing a Java interface and specifying it in a
  configuration file.
* Flexible experiment and round scoped parameterization via [Java properties files](http://docs.oracle.com/javase/7/docs/api/java/util/Properties.html) used to define experimental treatments.
  Experiment scoped parameters apply to the entire experiment whereas round scoped parameters apply to a specific round.
* Support for interactive quizzes, monitored real-time chat with options for censoring, voting, sanctioning, mini trust
  games in between rounds, embedded links that uniquely and anonymously identify each participant to Qualtrics surveys
  and limited fields of vision.
* Binary and XML savefiles that store every user action (e.g., movement, harvest events, sanction events, chats) in a time
  ordered stream that can be converted to a variety of CSV statistics files and into QuickTime movies. Custom data
  analysis can be done by extending the SaveFileConverter class and analyzing the OrderedSet of user actions.

### how to run the software

Quickstart:

* [install and setup Java, Ant, and Maven](https://bitbucket.org/virtualcommons/sesef/wiki/Home)
* [download and unpack the foraging codebase](https://bitbucket.org/virtualcommons/foraging/downloads)
* From the command-line (e.g., Windows PowerShell or Command Prompt, Mac OSX Terminal.app, or any Linux terminal)
```
#!bash
% ant prepare-demo
% ant demo % this will start a server, a facilitator, and 5 clients in the demo treatment
```
For more detailed instructions, please see the [installation instructions on our wiki](https://bitbucket.org/virtualcommons/foraging/wiki/Installation).

### publications

Data generated from the foraging framework has been published in 
[Lab Experiments for the Study of Social-Ecological Systems](http://www.sciencemag.org/cgi/content/abstract/328/5978/613). 
Archives of the [the configuration files used](https://bitbucket.org/virtualcommons/foraging/src/tip/src/main/resources/configuration/iu/archived/) and the
[generated data](http://dev.commons.asu.edu/data/foraging/2008/all-iu-foraging-data.zip) are available.

### status
This project is not under active development but is actively maintained. If you'd like to add new features or find any
bugs, please [let us know](http://vcweb.asu.edu/contact).
