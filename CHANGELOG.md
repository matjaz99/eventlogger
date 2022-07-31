# EventLogger changelog

## 0.2.2

* [FEATURE] Added HTTP webhook for any kind of events with plaintext in the body

* [DOCKER] `docker pull matjaz99/eventlogger:0.2.2`
* [FIX] Filtering for memory storage

## 0.2.1 Filtering events / 2022-07-25

* [DOCKER] `docker pull matjaz99/eventlogger:0.2.1`
* [FEATURE] Filter events by hostname and ident

## 0.2.0 MongoDB storage / 2022-07-19

* [DOCKER] `docker pull matjaz99/eventlogger:0.2.0`
* [FEATURE] Store and load events in MongoDB

## 0.1.0 Proof of concept / 2022-07-11

* [DOCKER] `docker pull matjaz99/eventlogger:0.1.0`
* [FEATURE] This is Java based web app running on Tomcat using JSF+Primefaces
* [FEATURE] Receive messages from fluentd (using in_syslog and out_http plugin)
* [FEATURE] Store last 1000 events in memory - this will remain default data storage (type and size)
* [FEATURE] Show list of events in web browser
* [FEATURE] Prepared Dockerfile, compose file and other stuff
