##### Link the sources to standard go workspace

```
export PROJECTS_PATH=~/code
mkdir $GOPATH/src/github.com/eclipse/che -p
ln -s $PROJECTS_PATH/che/exec-agent $GOPATH/src/github.com/eclipse/che/exec-agent
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
