Websocket API
---
[JSON RPC 2.0](http://www.jsonrpc.org/specification) protocol is used for client-server
communication, but:
- `params` is always json object(never array)
- server to client notifications are treated as [Events](events.md)

the apis described below include some of the following fields:
```json
{
  "jsonrpc" : "2.0",
  "method": "...",
  "id": "...",
  "params": { },
  "error" : { },
  "result" : { }
}
```
 these fields are part of the protocol so they are not documented.

## Process API


### Start process

##### Request

- __name__ - the name of the command
- __commandLine__ - command line to execute
- __type__(optional) - command type
- __eventTypes__(optional) - comma separated types of events which will be
 received by this channel. By default all the process events will be received.
Possible values are: `stderr`, `stdout`, `process_status`

```json
{
  "method": "process.start",
  "id": "id1234567",
  "params": {
    "name": "print",
    "commandLine": "printf \"1\n2\n3\"",
    "type": "test"
  }
}
```

##### Response

```json
{
  "jsonrpc": "2.0",
  "id": "id1234567",
  "result": {
    "pid": 1,
    "name": "print",
    "commandLine": "printf \"1\n2\n3\"",
    "type": "test",
    "alive": true,
    "nativePid": 19920
  }
}
```

#### Errors

- when either `name` or `commandLine` is missing, e.g:

```json
{
  "jsonrpc": "2.0",
  "id": "id1234567",
  "error": {
    "code": -32602,
    "message": "Command line required"
  }
}
```

### Kill process

##### Request

- __pid__ - the id of the process to kill

```json
{
  "method": "process.kill",
  "id": "id1234567",
  "params": {
    "pid": 2
  }
}
```

##### Response

```json
{
  "jsonrpc": "2.0",
  "id": "id1234567",
  "result": {
    "pid": 2,
    "text": "Successfully killed"
  }
}
```

##### Errors

- when there is no such process

```json
{
  "jsonrpc": "2.0",
  "id": "id1234567",
  "error": {
    "code": -32000,
    "message": "Process with id '2' does not exist"
  }
}
```

- when process with given id is not alive

```json
{
  "jsonrpc": "2.0",
  "id": "id1234567",
  "error": {
    "code": -32001,
    "message": "Process with id '2' is not alive"
  }
}
```

### Subscribe to process events

##### Request

- __pid__ - the id of the process to subscribe to
- __eventTypes__(optional) - comma separated types of events which will be
received by this channel. By default all the process events will be received,
not supported even types are ignored. Possible values are: `stdout`, `stderr`, `process_status`.
- __after__(optional) - process logs which appeared after given time will
be republished to the channel. This parameter may be useful when reconnecting to the exec-agent

```json
{
  "method": "process.subscribe",
  "id": "0x12345",
  "params": {
    "pid": 2,
    "eventTypes": "stdout,stderr",
    "after" : "2016-07-26T09:36:44.920890113+03:00"
  }
}
```        


##### Response

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "result": {
    "pid": 2,
    "eventTypes": "stdout,stderr",
    "text": "Successfully subscribed"
  }
}
```

##### Errors

- when there is no a single valid event type provided

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32602,
    "message": "Required at least 1 valid event type"
  }
}
```

- when `after` is not in valid format

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32602,
    "message": "Bad format of 'after', parsing time \"2016-07-26\" as \"2006-01-02T15:04:05.999999999Z07:00\": cannot parse \"\" as \"T\""
  }
}
```

- when this channel is already subscribed on process events

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32603,
    "message": "Already subscribed"
  }
}
```

- when there is no such process

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32000,
    "message": "Process with id '2' does not exist"
  }
}
```

- when process with given id is not alive

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32001,
    "message": "Process with id '2' is not alive"
  }
}
```



### Unsubscribe from process events

##### Request

- __pid__ - the id of the process to unsubscribe from

```json
{
  "method": "process.unsubscribe",
  "id": "0x12345",
  "params": {
    "pid": 2
  }
}
```

##### Response

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "result": {
    "pid": 2,
    "text": "Successfully unsubscribed"
  }
}
```

##### Errors

- when there is no such process

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32000,
    "message": "Process with id '2' does not exist"
  }
}
```

- when process with given id is not alive

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32001,
    "message": "Process with id '2' is not alive"
  }
}
```



### Update process subscriber

##### Request

- __pid__ - the id of the process which subscriber should be updated
- __eventTypes__ -  comma separated types of events which will be
received by this channel. Not supported even types are ignored.
Possible values are: `stdout`, `stderr`, `process_status`.

```json
{
  "method": "process.updateSubscriber",
  "id": "0x12345",
  "params": {
    "pid": 2,
    "eventTypes": "process_status"
  }
}
```

##### Response

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "result": {
    "pid": 2,
    "eventTypes": "process_status",
    "text": "Subscriber successfully updated"
  }
}
```

##### Errors

- when there is no such process

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32000,
    "message": "Process with id '2' does not exist"
  }
}
```

- when process with given id is not alive

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32001,
    "message": "Process with id '2' is not alive"
  }
}
```

- when this channel is not subscribed on process events

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32603,
    "message": "No subscriber with id 'channel-1'"
  }
}
```


### Get process logs

##### Request

- __pid__ - the id of the process to get logs
- __from__(optional) - time to get logs from e.g. _2016-07-12T01:48:04.097980475+03:00_ the format is _RFC3339Nano_
- __till__(optional) - time to get logs till e.g. _2016-07-12T01:49:04.097980475+03:00_ the format is _RFC3339Nano_
- __limit__(optional) - the limit of logs in result, the default value is _50_, logs are limited from the
latest to the earliest
- __skip__ (optional) - the logs to skip, default value is `0`

```json
{
  "method": "process.getLogs",
  "id": "0x12345",
  "params": {
    "pid": 3,
    "limit": 5,
    "skip": 5
  }
}
```

##### Response

For the command `printf "1\n2\n3\n4\n5\n6\n7\n8\n9\n10`, the result will look like

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "result": [
    {
      "kind": "STDOUT",
      "time": "2016-09-24T17:18:30.757623274+03:00",
      "text": "1"
    },
    {
      "kind": "STDOUT",
      "time": "2016-09-24T17:18:30.757701555+03:00",
      "text": "2"
    },
    {
      "kind": "STDOUT",
      "time": "2016-09-24T17:18:30.757721423+03:00",
      "text": "3"
    },
    {
      "kind": "STDOUT",
      "time": "2016-09-24T17:18:30.757841518+03:00",
      "text": "4"
    },
    {
      "kind": "STDOUT",
      "time": "2016-09-24T17:18:30.757851622+03:00",
      "text": "5"
    }
  ]
}
```

##### Errors


- when there is no such process

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32000,
    "message": "Process with id '2' does not exist"
  }
}
```

- when one of the time parameters is invalid, e.g:

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32602,
    "message": "Bad format of 'till', parsing time \"date\" as \"2006-01-02T15:04:05.999999999Z07:00\": cannot parse \"date\" as \"2006\""
  }
}
```



### Get process

##### Request

- __pid__ - the id of the process to get

```json
{
  "method": "process.getProcess",
  "id": "0x12345",
  "params": {
    "pid": 3
  }
}
```

##### Response

When everything is okay

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "result": {
    "pid": 1,
    "name": "print",
    "commandLine": "printf \n1\n2\n3\"",
    "type": "test",
    "alive": false,
    "nativePid": 13158
  }
}    
```

##### Errors


- when there is no such process

```json
{
  "jsonrpc": "2.0",
  "id": "0x12345",
  "error": {
    "code": -32000,
    "message": "Process with id '2' does not exist"
  }
}
```


### Get processes

##### Request

- __all__(optional) - if `true` then all the processes including _dead_ ones will be returned,
otherwise only _alive_ processes will be returned

```json
{
  "method": "process.getProcesses",
  "id": "id1234567",
  "params": {
    "all": true
  }
}
```

##### Response

```json
{
  "jsonrpc": "2.0",
  "id": "id1234567",
  "result": [
    {
      "pid": 1,
      "name": "print",
      "commandLine": "printf \"1\n2\n3\"",
      "type": "test",
      "alive": false,
      "nativePid": 13553
    },
    {
      "pid": 2,
      "name": "print2",
      "commandLine": "printf \"\n3\n2\n1\"",
      "type": "test2",
      "alive": false,
      "nativePid": 13561
    }
  ]
}
```
