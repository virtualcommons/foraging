FROM frekele/ant:1.9.7-jdk8

COPY . /code
WORKDIR /code
RUN ant deploy

CMD ["/usr/bin/java", "-jar", "-server", "server.jar"]

