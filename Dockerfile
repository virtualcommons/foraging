FROM comses/ant:1.10

LABEL maintainer="Allen Lee <allen.lee@asu.edu>, Center for Behavior, Institutions and the Environment <cbie.asu.edu>"

USER root
COPY docker/services/experiment.sh /etc/service/foraging/run
RUN chmod a+rx -R /etc/service/foraging
COPY src /code/src
COPY build.properties *.xml /code/
RUN ant compile
