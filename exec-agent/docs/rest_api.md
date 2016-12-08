REST API
===

Process API
---

### Start a new process

#### Request

_POST /process_

- `channel`(optional) - the id of the channel which should be subscribed to the process events
- `types`(optional) - comma separated types works only in couple with specified `channel`, defines
the events which will be sent by the process to the `channel`. Several values may be specified,
e.g. `channel=channel-1&types=stderr,stdout`. By default channel will be subscribed to 
all the existing types(listed below). Possible type values:
    - `stderr` - output from the process stderr
    - `stdout` - output from the process stdout
    - `process_status` - the process status events(_started, died_)


```json
{
    "name" : "build",
    "commandLine" : "mvn clean install",
    "type" : "maven"
}
```

#### Response

```json
{
    "pid": 1,
    "name": "build",
    "commandLine": "mvn clean install",
    "type" : "maven",
    "alive": true,
    "nativePid": 9186
}
```
- `200` if successfully started
- `400` if incoming data is not valid e.g. name is empty
- `404` if specified `channel` doesn't exist
- `500` if any other error occurs


### Get a process

#### Request

_GET /process/{pid}_

- `pid` - the id of the process to get

#### Response

```json
{
    "pid": 1,
    "name": "build",
    "commandLine": "mvn clean install",
    "type" : "maven",
    "alive": false,
    "nativePid": 9186,
}
```

- `200` if response contains requested process
- `400` if `pid` is not valid, unsigned int required
- `404` if there is no such process
- `500` if any other error occurs

### Kill a process

#### Request

_DELETE /process/{pid}_

- `pid` - the id of the process to kill

#### Response

```json
{
    "pid": 1,
    "name": "build",
    "commandLine": "mvn clean install",
    "type" : "maven",
    "alive": true,
    "nativePid": 9186,
}
```
- `200` if successfully killed
- `400` if `pid` is not valid, unsigned int required
- `404` if there is no such process
- `500` if any other error occurs


### Get process logs

#### Request

_GET /process/{pid}/logs_

- `pid` - the id of the process to get logs
- `from`(optional) - time to get logs from e.g. _2016-07-12T01:48:04.097980475+03:00_ the format is _RFC3339Nano_
don't forget to encode this query parameter
- `till`(optional) - time to get logs till e.g. _2016-07-12T01:49:04.097980475+03:00_ the format is _RFC3339Nano_
don't forget to encode this query parameter
- `format`(optional) - the format of the response, default is `json`, possible values are: `text`, `json`
- `limit`(optional) - the limit of logs in result, the default value is _50_, logs are limited from the 
latest to the earliest
- `skip` (optional) - the logs to skip, default value is `0`

#### Response

The result logs of the process with the command line `printf "Hello\nWorld\n"`

Text:
```text
[STDOUT] 2016-07-04 08:37:56.315082296 +0300 EEST 	 Hello
[STDOUT] 2016-07-04 08:37:56.315128242 +0300 EEST 	 World
```

Json:
```json
[
    {
        "Kind" : "STDOUT",
        "Time" : "2016-07-16T19:51:32.313368463+03:00",
        "Text" : "Hello"
    },
    {
        "Kind" : "STDOUT",
        "Time" : "2016-07-16T19:51:32.313603625+03:00",
        "Text" : "World"
    }
]
```

- `200` if logs are successfully fetched
- `400` if `from` or `till` format is invalid
- `404` if there is no such process
- `500` if any other error occurs

### Get processes

#### Request

_GET /process_

- `all`(optional) - if `true` then all the processes including _dead_ ones will be returned(respecting paging ofc), 
otherwise only _alive_ processes will be returnedg

#### Response

The result of the request _GET /process?all=true_
```json
[
    {
        "pid": 1,
        "name": "build",
        "commandLine": "mvn clean install",
        "type" : "maven",
        "alive": true,
        "nativePid": 9186,
    },
    {
        "pid": 2,
        "name": "build",
        "commandLine": "printf \"Hello World\"",
        "alive": false,
        "nativePid": 9588
    }
]
```
- `200` if processes are successfully retrieved
- `500` if any error occurs

### Subscribe to the process events

#### Request

_POST /process/{pid}/events/{channel}_

- `pid` - the id of the process to subscribe to
- `channel` - the id of the webscoket channel which is subscriber
- `types`(optional) - the types of the events separated by comma e.g. `?types=stderr,stdout`
-  `after`(optional) - process logs which appeared after given time will
be republished to the channel. This method may be useful in the reconnect process

#### Response

- `200` if successfully subscribed
- `400` if any of the parameters is not valid
- `404` if there is no such process or channel
- `500` if any other error occurs

### Unsubscribe from the process events

#### Request

_DELETE /process/{pid}/events/{channel}_

- `pid` - the id of the process to unsubscribe from
- `channel` - the id of the webscoket channel which currenly subscribed
to the process events

#### Response

- `200` if successfully unsubsribed
- `400` if any of the parameters is not valid
- `404` if there is no such process or channel
- `500` if any other error occurs

### Update the process events subscriber

#### Request

_PUT /process/{pid}/events/{channel}_

- `pid` - the id of the process
- `channel` - the id of the websocket channel which is subscriber
- `types` - the types of the events separated with comma e.g. `?types=stderr,stdout`

#### Response

- `200` if successfully updated
- `400` if any of the parameters is not valid
- `404` if there is no such process or channel
- `500` if any other error occurs
