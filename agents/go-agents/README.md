Summary
---
Golang based server for executing commands and streaming process output logs,
also websocket-terminal.


Requirements
--
- golang 1.6+


Docs
---
- jsonrpc2.0 based [Websocket API](docs/ws_api.md)
- jsonrpc2.0 based [Events](docs/events.md)
- [REST API](docs/rest_api.md)

Development
---

##### Link the sources to standard go workspace

```bash
export CHE_PATH=~/code/che
mkdir $GOPATH/src/github.com/eclipse/che/agents/go-agents/src/main -p
ln -s $CHE_PATH/agents/go-agents/src/main/go $GOPATH/src/github.com/eclipse/che/agents/go-agents/src/main/go
```

That's it, `$GOPATH/src/github.com/eclipse/che/agents/go-agents` project is ready.

##### Building linked project

```bash
cd $GOPATH/src/github.com/eclipse/che/agents/go-agents/src/main/go && go build ./...
```

#### Building exec agent executable

```bash
cd $GOPATH/src/github.com/eclipse/che/agents/go-agents/src/main/go/exec-agent && go build
```

##### Running linked project tests

```bash
cd $GOPATH/src/github.com/eclipse/che/agents/go-agents/src/main/go && go test ./...
```

##### Formatting linked project sources

```bash
cd $GOPATH/src/github.com/eclipse/che/agents/go-agents/src/main/go && go fmt ./...
```
