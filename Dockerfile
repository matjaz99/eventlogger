# Build stage
FROM maven:3.8.1-openjdk-11 AS build

ARG SIMPLE_LOGGER_VERSION=1.7.0

RUN wget http://matjazcerkvenik.si/download/simple-logger-${SIMPLE_LOGGER_VERSION}.jar
RUN mvn install:install-file \
    -Dfile=simple-logger-${SIMPLE_LOGGER_VERSION}.jar \
    -DgroupId=si.matjazcerkvenik.simplelogger \
    -DartifactId=simple-logger \
    -Dversion=${SIMPLE_LOGGER_VERSION} \
    -Dpackaging=jar
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM tomcat:8.5-jre8-alpine
#FROM tomcat:9.0.64-jre11-openjdk

RUN apk --no-cache add curl
RUN apk add tzdata

COPY --from=build  /home/app/target/eventlogger.war /usr/local/tomcat/webapps/eventlogger.war

RUN mkdir -p /opt/eventlogger/log
COPY LICENSE /opt/eventlogger
COPY README.md /opt/eventlogger

EXPOSE 8080

