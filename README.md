### foraging

The foraging experiment is a common pool resource experiment where participants are placed in randomized groups and
interact with a spatially explicit renewable resource. It is built on the
[Social Ecological Systems Experiment Framework](http://github.com/virtualcommons/sesef) and has been used to conduct research studies
at [Arizona State University](http://www.asu.edu), [Indiana University](http://www.iu.edu), and the [University of Alaska-Anchorage](http://www.uaa.alaska.edu).

### features

* Participants move and interact with a resource in a real-time 2-D grid environment. 
* Pluggable resource growth dynamics. The default is a parameterized density-dependent growth function but there
  are also "patchy" resource growth dynamics where the top half of the grid has a higher chance of regrowth than the
  bottom half. Arbitrary regrowth dynamics are possible by implementing a Java interface and specifying it in a
  configuration file.
* Flexible experiment and round scoped parameterization via [Java properties files](http://docs.oracle.com/javase/8/docs/api/java/util/Properties.html) used to define experimental treatments.
  Experiment scoped parameters apply to the entire experiment whereas round scoped parameters apply to a specific round.
* Support for interactive quizzes, monitored real-time chat with options for censoring, voting, sanctioning, mini trust
  games in between rounds, embedded links that uniquely and anonymously identify each participant to Qualtrics surveys
  and limited fields of vision.
* Binary and XML savefiles that store every user action (e.g., movement, harvest events, sanction events, chats) in a time
  ordered stream that can be converted to a variety of CSV statistics files and into QuickTime movies. Custom data
  analysis can be done by extending the SaveFileConverter class and analyzing the OrderedSet of user actions.

### how to run the software

* [clone the foraging codebase with git](https://github.com/virtualcommons/foraging) via 
`git clone https://github.com/virtualcommons/foraging.git` or [download and unpack the latest release](https://github.com/virtualcommons/foraging/releases)
* customize `build.properties` from the `build.properties.example` file. At a minimum, make sure you set the
  `server.address` to the IP address or fully qualified hostname of the machine you are using to serve this application
* add configuration files to `src/main/resources/configuration`, see the [wiki's Configuration page](https://github.com/virtualcommons/foraging/wiki/Configuration) for more details.

#### use docker-compose

* `docker-compose run --service-ports experiment` will build an Docker image and start two containers: an experiment
  server listening on port 16001 and an nginx webserver listening on port 8080 to deliver the foraging client and
  facilitator applications via Java WebStart. The relevant URLs are `http://<server.address>:8080/` to start a WebStart client and
  `http://<server.address>:8080/facilitator.jnlp` to start a WebStart facilitator.
* *Back up your data:*  you are done running an experiment, make sure you back up the binary data saved in `experiment-data`. 
* Convert binary data stored in `DATA_DIR` to a variety of plaintext files via `docker-compose run data` or customize the statistics you see by writing a custom `SaveFileProcessor` and adding it to the list in `ForagingSaveFileConverter`. Run data conversion via `docker-compose run data`. You can select the data directory with the `DATA_DIR` environment variable and convert the XML savefiles with the `XML` environment variable, e.g., `docker-compose run -e DATA_DIR=<data-directory> -eXML=xml data`.
* Run `docker-compose down` to clean up your docker images when you're done.

#### or install everything locally

* [install and setup Java, Ant, and Maven](https://github.com/virtualcommons/sesef/wiki/Home)
* From the command-line (e.g., Windows PowerShell or Command Prompt, Mac OSX Terminal.app, or any Linux terminal)
```
% ant prepare-demo
% ant demo % this will start a server, a facilitator, and 5 clients in the demo treatment
```
For more detailed instructions, please see the [installation instructions on our wiki](https://github.com/virtualcommons/foraging/wiki/Installation).

### publications

Data generated from the foraging framework has been published in 
[Lab Experiments for the Study of Social-Ecological Systems](http://www.sciencemag.org/cgi/content/abstract/328/5978/613). 
Archives of the [configuration files used](https://github.com/virtualcommons/foraging/tree/master/src/main/resources/configuration/replication/2010/janssen-et-al) and the
[experiment data](https://osf.io/mdhb7) are also available.

### status
[![Build Status](https://travis-ci.org/virtualcommons/foraging.svg?branch=master)](https://travis-ci.org/virtualcommons/foraging)

If you'd like to add new features or find any bugs, please [let us know](http://vcweb.asu.edu/contact).
