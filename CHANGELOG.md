# EventLogger changelog

## 0.3.4-SNAPSHOT

* [FEATURE] Sort events in queue and flush data in periodic interval
* [FEATURE] Download content
* [ENHANCEMENT] Added hits counter for each rule and reset hits button

## 0.3.3-SNAPSHOT

* [FEATURE] Display pattern

## 0.3.1-SNAPSHOT

* [FEATURE] Querying OpenSearch

## 0.3.0-SNAPSHOT

* [FEATURE] OpenSearch client, send bulk

## 0.2.6 / 2023-11-03

* [ENHANCEMENT] Added `alarm destination` configuration
* [ENHANCEMENT] Added more help (syslog, fluentd)

## 0.2.5 / 2023-05-23

* [CHANGE] Renamed endpoint `event/*` endpoint back to `webhook/*`. It didn't sound better. 
Now all events are received on `/event/webhook/<name>` endpoint.
* [FEATURE] Home view with statistical charts about incoming events
* [FEATURE] Event rules view
* [ENHANCEMENT] Show ajax status when loading events from DB
* [ENHANCEMENT] Added `Home` icon in toolbar
* [ENHANCEMENT] Added new parameter to event: `logfile` - originating log file.
* [FIX] Corrected MongoDB query that contains Grok pattern. Grok pattern is converted to RegEx.
* [FIX] Corrections for raising and clearing alarms
* [FIX] Fixed metrics for ignored events and requests


## 0.2.4 / 2023-03-28

* [DOCKER] `docker pull matjaz99/eventlogger:0.2.4`
* [FEATURE] Event rules

## 0.2.3

* [DOCKER] `docker pull matjaz99/eventlogger:0.2.3`
* [CHANGE] Renamed `webhook/*` endpoint to `event/*`. Sounds better.
* [FEATURE] Help view
* [FEATURE] Sending alarms

## 0.2.2 HTTP webhook / 2022-08-21

* [DOCKER] `docker pull matjaz99/eventlogger:0.2.2`
* [CHANGE] Removed `webhook/` endpoint. Instead, use either `webhook/fluentd-syslog/` or `webhook/http/`
* [FEATURE] Added HTTP webhook for any kind of events with plaintext in the body or URL
* [ENHANCEMENT] Create index in MongoDB for `host` and `ident`
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
