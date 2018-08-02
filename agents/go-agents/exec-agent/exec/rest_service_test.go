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

package exec

import (
	"bytes"
	"encoding/json"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"os"
	"strconv"
	"strings"
	"testing"
	"time"

	"github.com/eclipse/che/agents/go-agents/core/process"
	"github.com/eclipse/che/agents/go-agents/core/process/processtest"
	"github.com/eclipse/che/agents/go-agents/core/rest"
	"net/url"
)

func TestStartProcessHandlerFunc(t *testing.T) {
	command := &process.Command{
		Name:        "test",
		CommandLine: "echo hello",
		Type:        "test",
	}
	req, err := http.NewRequest("POST", "/process", asJSONReader(t, command))
	if err != nil {
		t.Fatal(err)
	}
	rr := httptest.NewRecorder()

	asHTTPHandlerFunc(startProcessHF).ServeHTTP(rr, req)

	if rr.Code != http.StatusOK {
		t.Errorf("Expected status code %d but got %d", http.StatusOK, rr.Code)
	}

	mp := &process.MachineProcess{}
	json.Unmarshal(rr.Body.Bytes(), mp)
	failIfDifferent(t, command.Name, mp.Name, "name")
	failIfDifferent(t, command.CommandLine, mp.CommandLine, "command-line")
	failIfDifferent(t, command.Type, mp.Type, "type")
	failIfDifferent(t, -1, mp.ExitCode, "exit-code")
	failIfFalse(t, mp.Pid > 0, "Pid > 0")
}

func TestStartProcessFailsIfCommandIsInvalid(t *testing.T) {
	invalidCommands := []*process.Command{
		{
			Name:        "test",
			CommandLine: "",
			Type:        "test",
		},
		{
			Name:        "",
			CommandLine: "echo test",
			Type:        "test",
		},
	}

	for _, command := range invalidCommands {
		req, err := http.NewRequest("POST", "/process", asJSONReader(t, command))
		if err != nil {
			t.Fatal(err)
		}
		rr := httptest.NewRecorder()

		asHTTPHandlerFunc(startProcessHF).ServeHTTP(rr, req)

		failIfDifferent(t, http.StatusBadRequest, rr.Code, "status-code")
	}
}

func TestGetsExistingProcess(t *testing.T) {
	exp := startAndWaitProcess(t, "echo hello")

	strPid := strconv.Itoa(int(exp.Pid))
	req, err := http.NewRequest("GET", "/process/"+strPid, nil)
	if err != nil {
		t.Fatal(err)
	}
	rr := httptest.NewRecorder()

	asHTTPHandlerFunc(getProcessHF, "pid", strPid).ServeHTTP(rr, req)

	failIfDifferent(t, 200, rr.Code, "status-code")

	res := &process.MachineProcess{}
	json.Unmarshal(rr.Body.Bytes(), res)
	failIfDifferent(t, exp.Pid, res.Pid, "pid")
	failIfDifferent(t, exp.Name, res.Name, "name")
	failIfDifferent(t, exp.CommandLine, res.CommandLine, "command-line")
	failIfDifferent(t, exp.Type, res.Type, "type")
	failIfDifferent(t, exp.NativePid, res.NativePid, "native-pid")
	failIfDifferent(t, false, res.Alive, "alive")
}

func TestReturnsNotFoundWhenNoProcess(t *testing.T) {
	strPid := "4444"
	req, err := http.NewRequest("GET", "/process/"+strPid, nil)
	if err != nil {
		t.Fatal(err)
	}
	rr := httptest.NewRecorder()

	asHTTPHandlerFunc(getProcessHF, "pid", strPid).ServeHTTP(rr, req)

	failIfDifferent(t, 404, rr.Code, "status-code")
}

func TestGetsNoAliveProcesses(t *testing.T) {
	startAndWaitProcess(t, "echo test1")
	startAndWaitProcess(t, "echo test2")

	req, err := http.NewRequest("GET", "/process", nil)
	if err != nil {
		t.Fatal(err)
	}
	rr := httptest.NewRecorder()

	asHTTPHandlerFunc(getProcessesHF).ServeHTTP(rr, req)

	failIfDifferent(t, 200, rr.Code, "status-code")
	mps := []process.MachineProcess{}
	json.Unmarshal(rr.Body.Bytes(), &mps)
	failIfDifferent(t, 0, len(mps), "processes slice len")
}

func TestGetsProcessLogs(t *testing.T) {
	dir, err := ioutil.TempDir(os.TempDir(), "exec-agent-text")
	if err != nil {
		t.Fatal(err)
	}
	process.SetLogsDir(dir)
	defer process.WipeLogs()

	outputLines := []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}
	mp := startAndWaitProcess(t, "printf \""+strings.Join(outputLines, "\n")+"\"")

	realLogs, err := process.ReadAllLogs(mp.Pid)
	if err != nil {
		t.Fatal(err)
	}

	type TestCase struct {
		expectedLogs []*process.LogMessage
		queryString  string
	}

	cases := []TestCase{
		{
			expectedLogs: realLogs[5:],
			queryString:  "limit=5",
		},
		{
			expectedLogs: realLogs[:5],
			queryString:  "skip=5",
		},
		{
			expectedLogs: realLogs[3:5],
			queryString:  "limit=2&skip=5",
		},
		{
			expectedLogs: make([]*process.LogMessage, 0),
			queryString:  "limit=2&skip=20",
		},
		{
			expectedLogs: realLogs[9:],
			queryString:  "limit=1",
		},
		{
			expectedLogs: realLogs[6:],
			queryString:  query("from", realLogs[6].Time.Format(process.DateTimeFormat)),
		},
		{
			expectedLogs: realLogs[6:8],
			queryString: query(
				"from", realLogs[6].Time.Format(process.DateTimeFormat),
				"till", realLogs[7].Time.Format(process.DateTimeFormat),
			),
		},
	}

	strPid := strconv.Itoa(int(mp.Pid))
	baseURL := "/process/" + strconv.Itoa(int(mp.Pid)) + "/logs?"

	for _, theCase := range cases {
		// fetch logs
		req, err := http.NewRequest("GET", baseURL+theCase.queryString, nil)
		if err != nil {
			t.Fatal(err)
		}
		rr := httptest.NewRecorder()
		asHTTPHandlerFunc(getProcessLogsHF, "pid", strPid).ServeHTTP(rr, req)

		// must be 200ok
		failIfDifferent(t, http.StatusOK, rr.Code, "status code")

		// check logs are the same to expected
		logs := []*process.LogMessage{}
		json.Unmarshal(rr.Body.Bytes(), &logs)
		failIfDifferent(t, len(theCase.expectedLogs), len(logs), "logs len")
		for i := 0; i < len(theCase.expectedLogs); i++ {
			failIfDifferent(t, *theCase.expectedLogs[i], *logs[i], "log messages")
		}
	}
}

func query(kv ...string) string {
	if len(kv) == 0 {
		return ""
	}
	values := url.Values{}
	for i := 0; i < len(kv); i += 2 {
		values.Add(kv[i], kv[i+1])
	}
	return values.Encode()
}

func asJSONReader(t *testing.T, v interface{}) *bytes.Reader {
	body, err := json.Marshal(v)
	if err != nil {
		t.Fatal(err)
	}
	return bytes.NewReader(body)
}

func startAndWaitProcess(t *testing.T, cmd string) process.MachineProcess {
	captor := processtest.NewEventsCaptor(process.DiedEventType)
	captor.Capture()

	pb := process.NewBuilder()
	pb.CmdLine(cmd)
	pb.SubscribeDefault("test", captor)

	mp, err := pb.Start()
	if err != nil {
		captor.Stop()
		t.Fatal(err)
	}

	if ok := <-captor.Wait(2 * time.Second); !ok {
		t.Errorf("Waited 2 seconds for process to finish, killing the process %d", mp.Pid)
		if err := process.Kill(mp.Pid); err != nil {
			t.Error(err)
		}
		t.FailNow()
	}

	return mp
}

func failIfDifferent(t *testing.T, expected interface{}, actual interface{}, context string) {
	if expected != actual {
		t.Fatalf("Expected to receive '%v' %s but received '%v'", expected, context, actual)
	}
}

func failIfFalse(t *testing.T, condition bool, context string) {
	if !condition {
		t.Fatalf("%s: false", context)
	}
}

func asHTTPHandlerFunc(f rest.HTTPRouteHandlerFunc, params ...string) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if err := f(w, r, newFakeParams(params...)); err != nil {
			rest.WriteError(w, err)
		}
	}
}

func newFakeParams(kv ...string) *fakeParams {
	params := &fakeParams{make(map[string]string)}
	for i := 0; i < len(kv); i += 2 {
		params.items[kv[i]] = kv[i+1]
	}
	return params
}

type fakeParams struct {
	items map[string]string
}

func (p *fakeParams) Get(key string) string {
	return p.items[key]
}
