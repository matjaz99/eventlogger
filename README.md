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



### HTTP webhook

Eventlogger provides a generic webhook for receiving any kind of message. 






