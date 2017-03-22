FROM comses/ant:latest

COPY . /code
WORKDIR /code
RUN ant deploy

CMD ["/usr/bin/java", "-jar", "-server", "server.jar"]
