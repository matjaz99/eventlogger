# eventlogger for logging events

[![Build Status](https://semaphoreci.com/api/v1/matjaz99-44/eventlogger/branches/main/shields_badge.svg)](https://semaphoreci.com/matjaz99-44/eventlogger)
[![GitHub release](https://img.shields.io/github/release/matjaz99/eventlogger.svg)](https://GitHub.com/matjaz99/eventlogger/releases/)
[![Docker Pulls](https://img.shields.io/docker/pulls/matjaz99/eventlogger.svg)](https://hub.docker.com/r/matjaz99/eventlogger)

Eventlogger is a collector of events received on the http webhook. Event is considered to be a simple message, such 
as syslog event. Basically a line of text with some additional data about the source who sent the event.

Eventlogger started as a syslog viewer, backed-up by a database. Fluentd is used to collect Syslog events and 
forward them to eventlogger using fluent's `out_http` plugin.

Eventlogger offers a nice web GUI where events can be visualized (in a textual format), searched and filtered.

Without any configuration, by default, eventlogger will store the events in memory without any persistance. The `memory` 
storage type is limited to the last 1000 events. If you don't need long-term persistance of events, and you just 
need to follow the last N events from various sources centralized in one place, memory option is totally fine.

Eventlogger supports MongoDB as a long-term storage. The `mongodb` storage type must be properly configured 
to enable storing data in MongoDB. See configuration HERE.

At the end, eventlogger is a proxy between an event sender and database and it relays on the configuration of the 
sender (eg. fluentd configuration). See examples of fluentd configuration that is supported by eventlogger - HERE.

## Webhooks

Webhooks are http endpoints where data from various sources is sent from. Each webhook endpoint implements 
specific parser and processing of the data. At the end, the data ends in normalized and uniform format in the
database.



### Event model

Each event in eventlogger consists of 3 basic fields:
- host - the source IP or hostname of the entity that sent the event
- ident - identifier of the process that triggered the event
- message - textual data, plaintext or json, depends on the process that generated the message

Timestamp is added as current time in UNIX format when event is received.
PID (process ID), but it is not always present in the event.
Tag is optional.

## Deploy

Run in docker container:

```
$ docker run -d TODO ...
```

Docker compose file example is also available on GitHub.


## Configuration

## Storage type

Eventlogger currently supports two storage types: `memory` or `mongodb`.

### Memory

Memory storage type stores all data internally in memory. It is limited to the last 1000 events.

### MongoDB



## Configuring data sources

### Fluentd

Eventlogger supports events received from fluent's `out_http` plugin, but the structure of 
output messages strongly depend on the source type.

Eventlogger supports the following data sources in fluentd:
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

Rules file is in yaml format.

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
- `alarm` - send event as alarm to another http endpoint (see alarms). TODO: define what is clear


## Alarms

When event rule is matched, alarm is sent and added to the list of currently active alarms. 

Eventlogger keeps current state of active alarms. Active alarms can be retrieved from eventlogger
on `/eventlogger/alarms` endpoint.

Events on the other side are sent every time when conditions in the event rule are met. Events are 
not added to the list of active alarms and cannot be retrieved from the /alarms endpoint.

Destination URL where alarms and events will be sent to is configurable via environment variable 
`EVENTLOGGER_ALARM_DESTINATION`. Default destination url for Eventlogger alarms is **Alertmonitor** 
on URL: `http://alertmonitor:8080/alertmonitor/webhook/eventlogger`. 

Webhook for Eventlogger must be configured in Alertmonitor to be able to receive alarms. 
Read more about Alertmonitor project here [link].


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
