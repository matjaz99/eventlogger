# Eventlogger

### What is event in Eventlogger?

Event consists of the following attributes:

- `ident` - identification of the process that sent the event
- `msg` - content of body in plain text
- `tag` - custom label
- `timestamp` - timestamp of event reception
- `host` - hostname where the event came from


## Webhooks

Webhook is an endpoint where HTTP requests are received. Each webhook uses a specific parser for that type of message. 
The following webhooks are supported:

#### HTTP Webhook

Http endpoint which receives any POST request with plain text in the body.

```
POST  /eventlogger/webhook/http?tag=json&ident=my-process
Body: plain text
```


Curl example:
```
$ TODO
```

If event is sent as GET request, then body message must be included as URI parameter `msg` and properly encoded 
to avoid problems with whitespaces.

Example:
```
GET  /eventlogger/webhook/http?tag=test&ident=my-process&msg=This is the text message
```

#### Fluentd syslog webhook

Fluentd syslog webhook accepts syslog messages sent from fluentd. 

```
POST /eventlogger/webhook/fluentd-syslog
```


Configure fluentd to receive syslog messages with `in_syslog` plugin and send the data to Eventlogger with `out_http` plugin.

```
<source>
  @type syslog
  port 10514
  bind 0.0.0.0
  <transport udp>
  </transport>
  add_remote_addr true
  tag syslog.udp
  <parse>
    @type syslog
    message_format auto
  </parse>
</source>

<filter syslog.**>
  @type record_transformer
  <record>
    tag "${tag}"
  </record>
</filter>

<match syslog.**>
  @type http
  endpoint http://eventlogger:8080/eventlogger/webhook/fluentd-syslog
  open_timeout 2
  <format>
    @type json
  </format>
  <buffer>
    flush_interval 5s
  </buffer>
</match>
```


#### Fluentd tail webhook

Use fluend to tail log files using in_tail plugin. This example sends data to two destinations. Ident parameter is provided in URL.

```
POST /eventlogger/webhook/fluentd-tail
```


Configure fluentd:

```
<source>
  @type tail
  <parse>
    @type none
  </parse>
  read_from_head true
  tag alertmonitor
  path /fluentd/var/log/alertmonitor.log
  pos_file /tmp/alertmonitor.log.pos
</source>

<match alertmonitor>
  @type copy
  <store>
    @type http
    endpoint http://eventlogger:8080/eventlogger/webhook/fluentd-tail?ident=alertmonitor&file=alertmonitor.log
    http_method post
    open_timeout 2
    headers {"user-agent":"monis-fluentd-tail", "source-file":"alertmonitor.log"}
    content_type "application/json"
    json_array true
    <format>
      @type json
    </format>
    <buffer>
      flush_interval 5s
    </buffer>
  </store>
  <store>
    @type http
    endpoint http://192.168.0.25:8080/eventlogger/webhook/fluentd-tail?ident=alertmonitor&file=alertmonitor.log
    http_method post
    open_timeout 2
    headers {"user-agent":"monis-fluentd-tail", "source-file":"alertmonitor.log"}
    content_type "application/json"
    json_array true
    <format>
      @type json
    </format>
    <buffer>
      flush_interval 5s
    </buffer>
  </store>
</match>
```




