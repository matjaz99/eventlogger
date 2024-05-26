# eventlogger for logging events

![GitHub Project](https://img.shields.io/badge/app-eventlogger-seagreen)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/matjaz99/eventlogger/docker-image.yml)
[![GitHub release](https://img.shields.io/github/release/matjaz99/eventlogger.svg)](https://GitHub.com/matjaz99/eventlogger/releases/)
[![Docker Pulls](https://img.shields.io/docker/pulls/matjaz99/eventlogger.svg)](https://hub.docker.com/r/matjaz99/eventlogger)

Eventlogger is a central log storage, event processor (mainly logs) with browsing, searching and filtering capabilities. 
Eventlogger listens for HTTP requests (containg **events**) on a webhook, processes the message body, 
stores data in database and at the end offers an overview of received events in a web GUI.

[Screenshots](/docs/screenshots/Screenshots.md)

### What is event?

Event could be any message transmitted over the network. So, basically it's just a String. Perfect, 
as I was initially mostly interested into syslog events (logs).

Eventlogger focuses on human-readable messages which might have some context and meaning of what is 
happening in our network/server infrastructure, collect those messages and make use of them.

Each event should provide at least information about:
- where it comes from (source IP address, aka. `host`)
- who sent the event (some process with a name, aka. `ident`)
- what happened (the message of the event, aka. `message` or shorter `msg`)

Eventlogger offers a web GUI where events can be displayed, searched and filtered.

Eventlogger supports customized rules, which can extract data from events and trigger actions

Eventlogger started as a syslog viewer, backed-up by a database. Fluentd is used to collect Syslog events and 
forward them to Eventlogger using fluent's `out_http` plugin.

At the end, eventlogger is a proxy between an event sender and database.
See examples of fluentd configuration that is supported by eventlogger - (TODO).

## Webhooks

Webhooks are http endpoints where events from different sources are received. Each webhook endpoint implements 
specific parser for processing of data. Data is then normalized in uniform format and stored in the
database.



### Event

Each event in eventlogger consists of the following fields:
- host - the source IP or hostname of the entity that sent the event
- ident - identifier of the process that triggered the event
- message - textual data, plaintext or json, depends on the process that generated the message
- timestamp - added as current time in UNIX format when event is received
- pid (process ID), if it is present in the event
- tag - optional tag

> Host, ident and message are mandatory elements in each event.

## Deploy

Run in docker container:

```
$ docker run -d -p 8080:8080 
--name eventlogger
--env EVENTLOGGER_EVENT_RULES_CONFIG_FILE=/opt/eventlogger/rules/event_rules.yml 
-v ./rules/event_rules.yml:/opt/eventlogger/rules/event_rules.yml 
matjaz99/eventlogger:0.2.4
```

To run Eventlogger with persistence capability, configure MongoDB connection string via environment variables - see below.

Docker compose file example is available on GitHub.


## Configuration

Without any configuration, by default, eventlogger will store the events in memory without any persistance. The `memory`
storage type is limited to the last 1000 events. If you don't need long-term persistance of events, and you just
need to follow the last N events from various sources centralized in one place, memory option is totally fine.

Eventlogger supports MongoDB as a long-term storage. The `mongodb` storage type must be configured
to enable storing data in MongoDB.

## Storage types

Eventlogger currently supports two storage types: `memory` or `mongodb`.

### Memory

Memory storage type stores all data internally in memory. It is limited to the last 1000 events.
Memory storage type does not require any configuration.

### MongoDB

MongoDB is configured with environment variables:

| EnvVar                                    | Description                                                                                                          |
|-------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| `EVENTLOGGER_STORAGE_TYPE`                | set to `mongodb` to enable persistence in MongoDB (default is `memory`).                                             |
| `EVENTLOGGER_MONGODB_CONNECTION_STRING`   | connection string to connect to MongoDB (default `mongodb://admin:mongodbpassword@mongovm:27017/?authSource=admin`). |
| `EVENTLOGGER_MONGODB_CONNECT_TIMEOUT_SEC` | connect timeout of MongoDB client (default is 5 seconds).                                                            |
| `EVENTLOGGER_MONGODB_READ_TIMEOUT_SEC`    | read timeout of MongoDB client (default is 30 seconds).                                                              |
| `EVENTLOGGER_DB_POOL_SIZE`                | number of MongoDB clients (default 3).                                                                               |
| `EVENTLOGGER_DATA_RETENTION_DAYS`         | how many days before data is deleted from database (default 30 days)                                                 |


## Configuring data sources

### Fluentd

Eventlogger supports events received from fluentd's `out_http` plugin. The structure of 
output messages slightly depends on the source input type.

Eventlogger supports the following incoming data sources in fluentd:
- syslog
- tail
- http

Examples of each fluentd configuration can be found here (move to docs).

### Generic http webhook

This is basic http webhook which receives any GET or POST http method with some data. The data is taken 
either from the body of the message (in post request) or from URL parameters in case of get request.


### Fluentd-syslog endpoint


### HTTP endpoint

Eventlogger provides a generic webhook for receiving any kind of message. 

GET /eventlogger/event/http

Expected URL params:
- `ident` - name of the process
- `tag` - custom tag
- `msg` or `message` - body of message (string; regardless of format; no parsing)

> Hint: Message in URL may not contain whitespaces. Replace them with %20.


POST /eventlogger/event/http

Expected URL params:
- `ident` - name of the process
- `tag` - custom tag

Message is encapsulated inside request body.

Body formats (based on content-type):
- `json` - application/json; could be object {} or array [{},{}]
- `ndjson` - application/x-ndjson; new-line separator between objects {}\n{} (or no separator at all: {}{})
- `plain-text` - text/plain; string; no parsing
- `xml` - application/xml; string; no parsing


## Rules

Rules are powerful and efficient way to check the contents of event message and act upon it. Rule specifies 
a search pattern and an action that must be executed when event matches that search pattern.

Rules file is in yaml format. Default location of rules file is `/opt/eventlogger/rules/event_rules.yml`, 
but it can be overridden with environment variable `EVENTLOGGER_EVENT_RULES_CONFIG_FILE`.

Rules are grouped into groups. Each group has a `name`, `endpoint` and a list of `rules`. 
The `endpoint` will limit the processing of rules only to events received on specified endpoint. 
See above for supported endpoints.

Each rule consists of `name`, `filter`, `pattern` and `action`. 

With `filter` it is possible to limit the rule to process only events that come from specific `host` or 
from a specific process (ie. `ident`). Using `filter` is optional.

A `pattern` requires an expression `expr` and the `type` of expression. Type could be either `regex` or `grok`. 
`regex` is a normal regular expression, while `grok` is the grok pattern which is *regex on steroids*. 

See here[link] for grok patterns.

The `action` element is mandatory and defines what action will be undertaken if event matches the specified 
expression and filter.
Possible values of `action` are:
- `count` - count events that match with the rule conditions and will be exposed as Prometheus metric (see metrics). 
Requires additional parameter `metricName`.
- `event` - forward event to another http endpoint (see alarms).
- `alarm` - send alarm notification to another http endpoint (see alarms).
- `clear` - send alarm-clear notification to another http endpoint. Rule name for clear must match rule name for alarm!


## Alarms and events

When event rule is matched, alarm is sent and added to the list of currently active alarms. 

Eventlogger keeps current state of active alarms. Active alarms can be retrieved from eventlogger
on `/eventlogger/alarms` endpoint.

Withdrawal of alarm is also supported. Rule name for clear must match rule name for alarm!

Events (aka. notifications) on the other side are sent every time when conditions in the event rule are met. Events are 
not added to the list of active alarms and cannot be retrieved from the /alarms endpoint.

Destination URL where alarms and events will be sent to is configurable via environment variable 
`EVENTLOGGER_ALARM_DESTINATION`. Default destination url for Eventlogger alarms is **Alertmonitor** 
on URL: `http://alertmonitor:8080/alertmonitor/webhook/eventlogger`. 

Eventlogger webhook must be configured in Alertmonitor to be able to receive alarms. 
Read more about Alertmonitor project on Github: [here](https://hub.docker.com/r/matjaz99/alertmonitor).


## Metrics

Eventlogger contains in-built exporter for exposing metric in Prometheus format.

Supported metrics:
- `eventlogger_build_info` (gauge)
- `eventlogger_http_requests_total` (counter)
- `eventlogger_http_requests_size_total` (counter)
- `eventlogger_events_total` (counter)
- `eventlogger_events_ignored_total` (counter)
- `eventlogger_db_duration_seconds` (histogram)
- `eventlogger_db_errors_total` (counter)
- `eventlogger_rule_actions_total` (counter)
- `eventlogger_rule_evaluation_seconds` (histogram)
- `eventlogger_memory_total_bytes` (gauge)
- `eventlogger_memory_free_bytes` (gauge)
- `eventlogger_memory_max_bytes` (gauge)
- `eventlogger_available_processors` (gauge)

Additional metrics can be defined through actions in the event rules.
