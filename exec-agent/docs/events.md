Events
===
Messages sent via websocket connections to clients

Process Events
---

#### STDERR event

Published when process writes to stderr.
One stderr event describes one output line

```json
{
    "type":"stderr",
    "time":"2016-08-04T03:07:27.079183894+03:00",
    "body":{
        "pid":3,
        "text":"sh: ifconfig: command not found\n"
    }
}
```

#### STDOUT event

Published when process writes to stdout.
One stdout event describes one output line

```json
{
    "type":"stdout",
    "time":"2016-08-04T03:08:48.126499411+03:00",
    "body":{
        "pid":4,
        "text":"Starting server..."
    }
}
```

#### Process started

Published when process is successfully started.
This is the first event from all the events produced by process,
it appears only once for one process

```json
{
    "type":"process_started",
    "time":"2016-08-04T03:08:48.124621585+03:00",
    "body":{
        "pid":4,
        "nativePid":21240,
        "name":"build",
        "commandLine":"mvn clean install"
    }
}
```

#### Process died

Published when process is done, or killed. This is the last event from the process,
it appears only once for one process

```json
{
    "type":"process_died",
    "time":"2016-08-04T03:08:48.126720857+03:00",
    "body":{
        "pid":4,
        "nativePid":21240,
        "name":"build",
        "commandLine":"mvn clean install"
    }
}
```

Channel Events
---

#### Connected

The first event in the channel, published when client successfully connected to the machine-agent.

```json
{
    "type":"connected",
    "time":"2016-08-04T02:59:46.224903844+03:00",
    "body":{
        "channel":"channel-1",
        "text":"Hello!"
    }
}
```
