#!/usr/bin/env bash

HOST=192.168.0.25
IDENT=curl-test

# there may not be any whitespaces in the url or you will get an error: HTTP/1.1 505 HTTP Version Not Supported
# this is annoying replacing all whitespaces with %20
curl -v "http://$HOST:8080/eventlogger/event/http?tag=json&ident=curl-test&msg=This%20is%20plain%20text%20message%20sent%20from%20curl"

# GET request (this will get HTTP/1.1 505 HTTP Version Not Supported error again because of whitespaces)
curl -v -G -d "ident=curl-test" -d "msg={\"key1\":\"To je text\",\"key2\":1234}" "http://$HOST:8080/eventlogger/event/http"

# this GET works
curl -v -G -d "ident=curl-test" -d "msg={\"key1\":\"To_je_text\",\"key2\":1234}" "http://$HOST:8080/eventlogger/event/http"

# POST request (same request is sent as POST)
curl -v -d "ident=curl-test" -d "msg={\"key1\":\"To je text\",\"key2\":1234}" "http://$HOST:8080/eventlogger/event/http"

# TODO this request does not appear in eventlogger
curl -v -d "ident=curl-test" -d "msg=To je text" "http://$HOST:8080/eventlogger/event/http"



curl -v --location --request POST "http://$HOST:8080/eventlogger/event/http?tag=json&ident=curl-test" \
      --header 'Content-Type: application/json' \
      --data-raw '{"time":"2023-02-19 18:11:35","text":"Matjaz was here"}'

# example of syslog message sent from fluentd out_http plugin (in nd-json format)
ND_JSON_MESSAGE='{"host":"lionvm","ident":"systemd","pid":"1","message":"run-docker-runtime\\x2drunc-moby-361398deff47d5f9543794f346c7a5d4974a1109fe458169a8740ce1adcc0600-runc.jFbLIg.mount: Succeeded.","tag":"syslog.udp.daemon.info"}{"host":"swarm2","ident":"node_exporter","pid":"892","message":"ts=2023-02-19T20:09:26.121Z caller=textfile.go:227 level=error collector=textfile msg=\"failed to collect textfile data\" file=directory_size.prom err=\"failed to parse textfile data from \\\"/etc/node-exporter/directory_size.prom\\\": text format parsing error in line 4: expected float as value, got \\\"\\\"\"","tag":"syslog.udp.daemon.info"}{"host":"swarm3","ident":"node_exporter","pid":"748","message":"ts=2023-02-19T20:09:27.113Z caller=textfile.go:227 level=error collector=textfile msg=\"failed to collect textfile data\" file=directory_size.prom err=\"failed to parse textfile data from \\\"/etc/node-exporter/directory_size.prom\\\": text format parsing error in line 4: expected float as value, got \\\"\\\"\"","tag":"syslog.udp.daemon.info"}{"host":"swarm2","ident":"node_exporter","pid":"892","message":"ts=2023-02-19T20:09:28.193Z caller=textfile.go:227 level=error collector=textfile msg=\"failed to collect textfile data\" file=directory_size.prom err=\"failed to parse textfile data from \\\"/etc/node-exporter/directory_size.prom\\\": text format parsing error in line 4: expected float as value, got \\\"\\\"\"","tag":"syslog.udp.daemon.info"}{"host":"swarm3","ident":"node_exporter","pid":"748","message":"ts=2023-02-19T20:09:28.288Z caller=textfile.go:227 level=error collector=textfile msg=\"failed to collect textfile data\" file=directory_size.prom err=\"failed to parse textfile data from \\\"/etc/node-exporter/directory_size.prom\\\": text format parsing error in line 4: expected float as value, got \\\"\\\"\"","tag":"syslog.udp.daemon.info"}{"host":"promvm","ident":"sshd","pid":"29981","message":"Did not receive identification string from 192.168.0.141 port 47968","tag":"syslog.udp.authpriv.info"}{"host":"swarm3","ident":"node_exporter","pid":"748","message":"ts=2023-02-19T20:09:31.759Z caller=textfile.go:227 level=error collector=textfile msg=\"failed to collect textfile data\" file=directory_size.prom err=\"failed to parse textfile data from \\\"/etc/node-exporter/directory_size.prom\\\": text format parsing error in line 4: expected float as value, got \\\"\\\"\"","tag":"syslog.udp.daemon.info"}'

curl -v --location --request POST "http://$HOST:8080/eventlogger/event/fluentd-syslog" \
      --header 'Content-Type: application/x-ndjson' \
      --data-raw $ND_JSON_MESSAGE
