#FROM tomcat:8.5-jre8-alpine
FROM tomcat:9.0-jre17

RUN apk --no-cache add curl
RUN apk add tzdata

COPY target/eventlogger.war /usr/local/tomcat/webapps/eventlogger.war

RUN mkdir -p /opt/eventlogger/log
COPY LICENSE /opt/eventlogger
COPY README.md /opt/eventlogger

#HEALTHCHECK --interval=5m --timeout=3s \
#  CMD curl -f http://localhost:8080/eventlogger || exit 1

EXPOSE 8080

