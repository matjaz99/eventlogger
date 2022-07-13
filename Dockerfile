#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
RUN curl http://matjazcerkvenik.si/download/simple-logger-1.7.0.jar -o simple-logger-1.7.0.jar
RUN mvn install:install-file \
    -Dfile=simple-logger-1.7.0.jar \
    -DgroupId=si.matjazcerkvenik.simplelogger \
    -DartifactId=simple-logger \
    -Dversion=1.7.0 \
    -Dpackaging=jar
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package
#
##
## Package stage
##
#FROM openjdk:11-jre-slim
#COPY --from=build /home/app/target/demo-0.0.1-SNAPSHOT.jar /usr/local/lib/demo.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","/usr/local/lib/demo.jar"]



FROM tomcat:8.5-jre8-alpine
#FROM tomcat:9.0-jre17

RUN apk --no-cache add curl
RUN apk add tzdata

COPY --from=build  /home/app/target/eventlogger.war /usr/local/tomcat/webapps/eventlogger.war

RUN mkdir -p /opt/eventlogger/log
COPY LICENSE /opt/eventlogger
COPY README.md /opt/eventlogger

#HEALTHCHECK --interval=5m --timeout=3s \
#  CMD curl -f http://localhost:8080/eventlogger || exit 1

EXPOSE 8080

