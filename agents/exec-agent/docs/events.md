Events
===
Messages sent via websocket connections to clients

Channel Events
---

#### Connected

The first event in the channel, published when client successfully connected to the exec-agent.

```json
{
  "jsonrpc": "2.0",
  "method": "connected",
  "params": {
    "time": "2016-09-24T16:40:05.098478609+03:00",
    "channel": "channel-1",
    "text": "Hello!"
  }
}
```

Process Events
---

#### Process started

Published when process is successfully started.
This is the first event from all the events produced by process,
it appears only once for one process

```json
{
  "jsonrpc": "2.0",
  "method": "process_started",
  "params": {
    "time": "2016-09-24T16:40:55.930743249+03:00",
    "pid": 1,
    "nativePid": 22164,
    "name": "print",
    "commandLine": "printf \"\n1\n2\n3\""
  }
}
```

#### STDOUT event

Published when process writes to stdout.
One stdout event describes one output line

```json
{
  "jsonrpc": "2.0",
  "method": "process_stdout",
  "params": {
    "time": "2016-09-24T16:40:55.933255297+03:00",
    "pid": 1,
    "text": "Starting server..."
  }
}
```

#### STDERR event

Published when process writes to stderr.
One stderr event describes one output line

```json
{
  "jsonrpc": "2.0",
  "method": "process_stderr",
  "params": {
    "time": "2016-09-24T16:40:55.933255297+03:00",
    "pid": 1,
    "text": "sh: ifconfig: command not found"
  }
}
```

#### Process died

Published when process is done, or killed. This is the last event from the process,
it appears only once for one process

```json
{
  "jsonrpc": "2.0",
  "method": "process_died",
  "params": {
    "time": "2016-09-24T16:40:55.93354086+03:00",
    "pid": 1,
    "nativePid": 22164,
    "name": "print",
    "commandLine": "printf \"\n1\n2\n3\""
  }
}
```
