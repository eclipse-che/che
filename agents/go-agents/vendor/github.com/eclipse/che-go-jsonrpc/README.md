Websocket API
---
[JSON RPC 2.0](http://www.jsonrpc.org/specification) protocol is used for client-server
communication, but:
- `params` is always json object(never array)
- server to client notifications are treated as Events

the apis include some of the following fields:
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



##### Websocket messages order

The order is respected
```
Message fragments MUST be delivered to the recipient in the order sent by the sender.
```
Helpful Sources
* https://tools.ietf.org/html/rfc6455 (search the sentence above)
* http://stackoverflow.com/questions/11804721/can-websocket-messages-arrive-out-of-order
* http://stackoverflow.com/questions/14287224/processing-websockets-messages-in-order-of-receiving
