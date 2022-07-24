# Fluentd example setup

Example how to configure and deploy fluentd, so it will send messages to eventlogger.

In `fluentd.conf` configure where to send messages:

```
endpoint http://192.168.0.16:8080/eventlogger/webhook
```

And deploy the fluentd stack on docker:

```
$ docker stack deploy -c compose-fluentd.yml fluentd
```

Goto:

http://localhost:8080/eventlogger/events


## Configure syslog


