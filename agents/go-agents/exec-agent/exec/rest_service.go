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
	"errors"
	"fmt"
	"io"
	"log"
	"math"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/eclipse/che-go-jsonrpc"
	"github.com/eclipse/che/agents/go-agents/core/process"
	"github.com/eclipse/che/agents/go-agents/core/rest"
	"github.com/eclipse/che/agents/go-agents/core/rest/restutil"
)

// HTTPRoutes provides all routes that should be handled by the process API
var HTTPRoutes = rest.RoutesGroup{
	Name: "Process Routes",
	Items: []rest.Route{
		{
			Method:     "POST",
			Name:       "Start Process",
			Path:       "/process",
			HandleFunc: startProcessHF,
		},
		{
			Method:     "GET",
			Name:       "Get Process",
			Path:       "/process/:pid",
			HandleFunc: getProcessHF,
		},
		{
			Method:     "DELETE",
			Name:       "Kill Process",
			Path:       "/process/:pid",
			HandleFunc: killProcessHF,
		},
		{
			Method:     "GET",
			Name:       "Get Process Logs",
			Path:       "/process/:pid/logs",
			HandleFunc: getProcessLogsHF,
		},
		{
			Method:     "GET",
			Name:       "Get Processes",
			Path:       "/process",
			HandleFunc: getProcessesHF,
		},
	},
}

func startProcessHF(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
	command := process.Command{}
	if err := restutil.ReadJSON(r, &command); err != nil {
		return err
	}
	if err := checkCommand(&command); err != nil {
		return rest.BadRequest(err)
	}

	pb := process.NewBuilder().Cmd(command)

	// If channel is provided then check whether it is ready to be
	// first process subscriber and use it if it is
	channelID := r.URL.Query().Get("channel")
	if channelID != "" {
		channel, ok := jsonrpc.Get(channelID)
		if !ok {
			m := fmt.Sprintf("Tunnel with id '%s' doesn't exist. Process won't be started", channelID)
			return rest.NotFound(errors.New(m))
		}
		eventsConsumer := &rpcProcessEventConsumer{channel}
		pb.Subscribe(channelID, parseTypes(r.URL.Query().Get("types")), eventsConsumer)
	}

	proc, err := pb.Start()
	if err != nil {
		return err
	}
	return restutil.WriteJSON(w, proc)
}

func getProcessHF(w http.ResponseWriter, r *http.Request, p rest.Params) error {
	pid, err := parsePid(p.Get("pid"))
	if err != nil {
		return rest.BadRequest(err)
	}

	proc, err := process.Get(pid)
	if err != nil {
		return asHTTPError(err)
	}
	return restutil.WriteJSON(w, proc)
}

func killProcessHF(w http.ResponseWriter, r *http.Request, p rest.Params) error {
	pid, err := parsePid(p.Get("pid"))
	if err != nil {
		return rest.BadRequest(err)
	}
	if err := process.Kill(pid); err != nil {
		return asHTTPError(err)
	}
	return nil
}

type getLogsParams struct {
	pid    uint64
	from   time.Time
	till   time.Time
	limit  int
	skip   int
	format string
}

func getProcessLogsHF(w http.ResponseWriter, r *http.Request, p rest.Params) error {
	logsParams, err := parseGetLogsParameters(r, p)
	if err != nil {
		return err
	}

	logs, err := process.ReadLogs(logsParams.pid, logsParams.from, logsParams.till)
	if err != nil {
		return asHTTPError(err)
	}

	len := len(logs)
	fromIdx := int(math.Max(float64(len-logsParams.limit-logsParams.skip), 0))
	toIdx := len - int(math.Min(float64(logsParams.skip), float64(len)))

	// Respond with an appropriate logs format, default json
	switch strings.ToLower(logsParams.format) {
	case "text":
		for _, item := range logs[fromIdx:toIdx] {
			line := fmt.Sprintf("[%s] %s \t %s\n", item.Kind, item.Time.Format(process.DateTimeFormat), item.Text)
			if _, err := io.WriteString(w, line); err != nil {
				log.Printf("Error occurs on writing logs of process %v into response. %s", logsParams.pid, err)
			}
		}
	default:
		return restutil.WriteJSON(w, logs[fromIdx:toIdx])
	}
	return nil
}

func parseGetLogsParameters(r *http.Request, p rest.Params) (*getLogsParams, error) {
	pid, err := parsePid(p.Get("pid"))
	if err != nil {
		return nil, rest.BadRequest(err)
	}

	// Parse 'from', if 'from' is not specified then read all the logs from the start
	// if 'from' format is different from the DATE_TIME_FORMAT then return 400
	from, err := process.ParseTime(r.URL.Query().Get("from"), time.Time{})
	if err != nil {
		return nil, rest.BadRequest(errors.New("Bad format of 'from', " + err.Error()))
	}

	// Parse 'till', if 'till' is not specified then 'now' is used for it
	// if 'till' format is different from the DATE_TIME_FORMAT then return 400
	till, err := process.ParseTime(r.URL.Query().Get("till"), time.Now())
	if err != nil {
		return nil, rest.BadRequest(errors.New("Bad format of 'till', " + err.Error()))
	}

	// limit logs from the latest to the earliest
	// limit - how many the latest logs will be present
	// skip - how many log lines should be skipped from the end
	limit := restutil.IntQueryParam(r, "limit", DefaultLogsPerPageLimit)
	skip := restutil.IntQueryParam(r, "skip", 0)
	if limit < 1 {
		return nil, rest.BadRequest(errors.New("Required 'limit' to be > 0"))
	}
	if skip < 0 {
		return nil, rest.BadRequest(errors.New("Required 'skip' to be >= 0"))
	}

	format := r.URL.Query().Get("format")

	return &getLogsParams{
		pid:    pid,
		from:   from,
		till:   till,
		limit:  limit,
		skip:   skip,
		format: format,
	}, nil
}

func getProcessesHF(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
	all, err := strconv.ParseBool(r.URL.Query().Get("all"))
	if err != nil {
		all = false
	}
	return restutil.WriteJSON(w, process.GetProcesses(all))
}

func asHTTPError(err error) error {
	if npErr, ok := err.(*process.NoProcessError); ok {
		return rest.NotFound(npErr)
	}
	return err
}
