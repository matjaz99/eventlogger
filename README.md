# eventlogger for logging events

[![Build Status](https://semaphoreci.com/api/v1/matjaz99/eventlogger/branches/master/shields_badge.svg)](https://semaphoreci.com/matjaz99/eventlogger)
[![GitHub release](https://img.shields.io/github/release/matjaz99/eventlogger.svg)](https://GitHub.com/matjaz99/eventlogger/releases/)
[![Docker Pulls](https://img.shields.io/docker/pulls/matjaz99/eventlogger.svg)](https://hub.docker.com/r/matjaz99/eventlogger)

Eventlogger collects events from fluentd (eg. log files) and visualizes them in web GUI.

## Deploy

Run in docker container:

```
$ docker run -d TODO ...
```

## Configuration


## Configuring data sources

### Fluentd

Eventlogger supports events received from fluent's `out_http` plugin, but the structure of 
output messages strongly depend on the source type. 

Eventlogger supports the following data sources in fluentd:
- syslog
- tail
- http

Examples of each fluentd configuration can be found here (move to docs).




### HTTP webhook

Eventlogger provides a generic webhook for receiving any kind of message. 
