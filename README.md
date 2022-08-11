# eventlogger for logging events

[![Build Status](https://semaphoreci.com/api/v1/matjaz99-44/eventlogger/branches/main/shields_badge.svg)](https://semaphoreci.com/matjaz99-44/eventlogger)
[![GitHub release](https://img.shields.io/github/release/matjaz99/eventlogger.svg)](https://GitHub.com/matjaz99/eventlogger/releases/)
[![Docker Pulls](https://img.shields.io/docker/pulls/matjaz99/eventlogger.svg)](https://hub.docker.com/r/matjaz99/eventlogger)

Eventlogger collects events from fluentd (eg. log files) and visualizes them in web GUI.

## Deploy

Run in docker container:

```
$ docker run -d TODO ...
```

## Configuration

## Storage type

Eventlogger currently supports two storage types: `memory` or `mongodb`.

### Memory

Memory storage type stores all data internally in memory. It is limited to the last 1000 events, 
but it works out-of-the-box without any configuration.

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
