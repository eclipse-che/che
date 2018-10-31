//
// Copyright (c) 2012-2018 Red Hat, Inc.
// This program and the accompanying materials are made
// available under the terms of the Eclipse Public License 2.0
// which is available at https://www.eclipse.org/legal/epl-2.0/
//
// SPDX-License-Identifier: EPL-2.0
//
// Contributors:
//   Red Hat, Inc. - initial API and implementation
//

package booter

import (
	"encoding/json"
	"testing"
	"time"
	"sync"

	"github.com/eclipse/che-go-jsonrpc/event"
	"github.com/eclipse/che-go-jsonrpc"
	"github.com/eclipse/che-go-jsonrpc/jsonrpctest"
)

var (
	testRuntimeID = RuntimeID{
		Workspace:   "my-workspace",
		Environment: "my-env",
		OwnerId:     "id",
	}
	testMachineName = "my-machine"

	print10numbersInst = Installer{
		ID:          "test-installer-1",
		Description: "Installer for testing",
		Version:     "1.0",
		Script:      "printf \"1\n2\n3\n4\n5\n6\n7\n8\n9\n10\"",
	}
	echoTestInst = Installer{
		ID:          "test-installer-2",
		Description: "Installer for testing",
		Version:     "1.0",
		Script:      "echo test",
	}
	print3numbersAndFailInst = Installer{
		ID:          "test-installer-3",
		Description: "Installer for testing",
		Version:     "1.0",
		Script:      "printf \"1\n\" && sleep 1 && printf \"error\" >&2 && exit 1",
	}
)

func TestBootstrap(t *testing.T) {
	// configuring bootstrapper
	runtimeID = testRuntimeID
	machineName = testMachineName
	installerTimeout = time.Second * 2
	installers = []Installer{print10numbersInst, echoTestInst}

	// configuring jsonrpc endpoint
	tunnel, cr, rr := jsonrpctest.NewTmpTunnel(2 * time.Second)
	defer tunnel.Close()
	defer rr.Close()
	PushStatuses(tunnel)
	PushLogs(tunnel, nil)

	startAndWaitInstallations(t)

	expectedEvents := []event.E{
		&StatusChangedEvent{Status: StatusStarting},

		&InstallerStatusChangedEvent{Status: InstallerStatusStarting, Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "1", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "2", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "3", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "4", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "5", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "6", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "7", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "8", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "9", Installer: print10numbersInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "10", Installer: print10numbersInst.ID},
		&InstallerStatusChangedEvent{Status: InstallerStatusDone, Installer: print10numbersInst.ID},

		&InstallerStatusChangedEvent{Status: InstallerStatusStarting, Installer: echoTestInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "test", Installer: echoTestInst.ID},
		&InstallerStatusChangedEvent{Status: InstallerStatusDone, Installer: echoTestInst.ID},

		&StatusChangedEvent{Status: StatusDone},
	}

	requests, err := cr.GetAllRequests()
	if err != nil {
		t.Fatal(err)
	}
	checkEvents(t, requests, expectedEvents...)
}

func TestBootstrapWhenItFails(t *testing.T) {
	// configuring bootstrapper
	runtimeID = testRuntimeID
	machineName = testMachineName
	installerTimeout = time.Second * 2
	installers = []Installer{print3numbersAndFailInst}

	// configuring jsonrpc endpoint
	tunnel, cr, rr := jsonrpctest.NewTmpTunnel(2 * time.Second)
	defer tunnel.Close()
	defer rr.Close()
	PushStatuses(tunnel)
	PushLogs(tunnel, nil)

	startAndWaitInstallations(t)

	expectedEvents := []event.E{
		&StatusChangedEvent{Status: StatusStarting},

		&InstallerStatusChangedEvent{Status: InstallerStatusStarting, Installer: print3numbersAndFailInst.ID},
		&InstallerLogEvent{Stream: StdoutStream, Text: "1", Installer: print3numbersAndFailInst.ID},
		&InstallerLogEvent{Stream: StderrStream, Text: "error", Installer: print3numbersAndFailInst.ID},

		&InstallerStatusChangedEvent{Status: InstallerStatusFailed, Installer: print3numbersAndFailInst.ID, Error: "error"},

		&StatusChangedEvent{Status: InstallerStatusFailed, Error: "error"},
	}

	requests, err := cr.GetAllRequests()
	if err != nil {
		t.Fatal(err)
	}
	checkEvents(t, requests, expectedEvents...)
}

func TestReconnect(t *testing.T) {
	// create tunnel & close it immediately
	closedTunnel := jsonrpc.NewManagedTunnel(jsonrpctest.NewConnRecorder())
	closedTunnel.Close()

	connector := newTestConnector()
	defer connector.doClose()

	tb := tunnelBroadcaster{
		tunnel:          closedTunnel,
		connector:       connector,
		reconnectPeriod: time.Microsecond,
		reconnectOnce:   &sync.Once{},
	}
	// force broadcaster to reconnect
	tb.Accept(&InstallerLogEvent{Text: "test"})

	// publish log events until stop is not published
	stopPublishing := make(chan bool)
	go func() {
		for {
			select {
			case <-stopPublishing:
				return
			default:
				bus.Pub(&InstallerLogEvent{Text: "test"})
			}
		}
	}()

	err := connector.connRec.WaitUntil(jsonrpctest.WriteCalledAtLeast(1))
	stopPublishing <- true
	if err != nil {
		t.Fatal("Didn't reconnect in time")
	}
}

func startAndWaitInstallations(t *testing.T) {
	complete := make(chan bool, 1)
	go func() {
		Start()
		complete <- true
	}()
	select {
	case <-time.After(time.Second * 2):
		t.Fatalf("Installation timeout is reached")
	case <-complete:
	}
}

func checkEvents(t *testing.T, requests []*jsonrpc.Request, expectedEvents ...event.E) {
	if len(requests) != len(expectedEvents) {
		t.Logf("Expected evens len is '%d' while published '%d'", len(expectedEvents), len(requests))
		t.Logf("Received events:")
		for _, req := range requests {
			t.Log(req.Method)
		}
		t.FailNow()
	}
	for i := range requests {
		request := requests[i]
		expectedEvent := expectedEvents[i]
		if request.Method != expectedEvent.Type() {
			t.Fatalf("Even type '%s' != '%s'", requests[i].Method, expectedEvents[i].Type())
		}

		switch expectedEvent.Type() {
		case StatusChangedEventType:
			actual := &StatusChangedEvent{}
			json.Unmarshal(request.Params, actual)
			checkStatusEvent(t, actual, expectedEvent.(*StatusChangedEvent))
		case InstallerStatusChangedEventType:
			actual := &InstallerStatusChangedEvent{}
			json.Unmarshal(request.Params, actual)
			checkInstallerStatusEvent(t, actual, expectedEvent.(*InstallerStatusChangedEvent))
		case InstallerLogEventType:
			actual := &InstallerLogEvent{}
			json.Unmarshal(request.Params, actual)
			checkLogEvent(t, actual, expectedEvent.(*InstallerLogEvent))
		}
	}
}

func checkStatusEvent(t *testing.T, actual, expected *StatusChangedEvent) {
	if expected.Status != actual.Status {
		t.Fatalf("Expected bootstrapper status '%s' but got '%s'", expected.Status, actual.Status)
	}
}

func checkInstallerStatusEvent(t *testing.T, actual, expected *InstallerStatusChangedEvent) {
	if expected.Status != actual.Status {
		t.Fatalf("Expected installer status '%s' but got '%s'", expected.Status, actual.Status)
	}
	if expected.Installer != actual.Installer {
		t.Fatalf("Expected installer id '%s' but got '%s'", expected.Installer, actual.Installer)
	}
}

func checkLogEvent(t *testing.T, actual, expected *InstallerLogEvent) {
	if expected.Text != actual.Text {
		t.Fatalf("Expected log text to be '%s' but got '%s'", expected.Text, actual.Text)
	}
	if expected.Stream != actual.Stream {
		t.Fatalf(
			"Expected stream '%s' for log message '%s' but got '%s'",
			expected.Stream,
			expected.Text,
			actual.Stream,
		)
	}
	if expected.Installer != actual.Installer {
		t.Fatalf("Expected installer id '%s' but got '%s'", expected.Installer, actual.Installer)
	}
}

func newTestConnector() *testConnector {
	c := &testConnector{}
	c.tunnel, c.connRec, c.reqRec = jsonrpctest.NewTmpTunnel(2 * time.Second)
	return c
}

type testConnector struct {
	connRec *jsonrpctest.ConnRecorder
	reqRec  *jsonrpctest.ReqRecorder
	tunnel  *jsonrpc.Tunnel
}

func (c *testConnector) Connect() (*jsonrpc.Tunnel, error) {
	return c.tunnel, nil
}

func (c *testConnector) doClose() {
	c.tunnel.Close()
	c.reqRec.Close()
}
