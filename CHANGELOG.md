# EventLogger changelog

## 0.3.1-SNAPSHOT

* [ENHANCEMENT] Rules view GUI improvements

## 0.3.0 / 2024-05-26

* [CHANGE] Upgraded simple-logger to 1.7.1
* [CHANGE] HTTP requests are not stored in DB anymore because some requests were too big and DB refuses
  to insert the data. Instead, http requests (including body) are now logged in separate log file (eventlogger-http-requests.log).
* [FEATURE] OpenSearch client, send bulk
* [BETA-FEATURE] Querying OpenSearch
* [FEATURE] Display pattern. Configuration parameter `EVENTLOGGER_GUI_DISPLAY_PATTERN`
* [FEATURE] Sort events in queue and flush data in periodic interval. Configuration parameter: `EVENTLOGGER_MONGODB_BULK_INSERT_MAX_SIZE`
* [FEATURE] Added metrics `eventlogger_db_buffer_size` and `eventlogger_db_bulk_insert_size`
* [FEATURE] Download content
* [FEATURE] EvGen to generate dummy log file, for testing purposes
* [ENHANCEMENT] Added hits counter for each rule and reset hits button

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
