##### Link the sources to standard go workspace

```
mkdir $GOPATH/src/github.com/eclipse/che -p
ln -s ~/code/che/exec-agent $GOPATH/src/github.com/eclipse/che/exec-agent
```

##### Install godep
```
go get github.com/tools/godep
```

##### Get all dependencies

```
cd $GOPATH/src/github.com/eclipse/che/exec-agent
$GOPATH/bin/godep restore
```
