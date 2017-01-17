//
// Copyright (c) 2012-2017 Codenvy, S.A.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Codenvy, S.A. - initial API and implementation
//

package process

import (
	"errors"
	"fmt"
	"github.com/eclipse/che/agents/exec-agent/rest"
	"github.com/eclipse/che/agents/exec-agent/rest/restutil"
	"github.com/eclipse/che/agents/exec-agent/rpc"
	"io"
	"math"
	"net/http"
	"strconv"
	"strings"
	"time"
)

var HttpRoutes = rest.RoutesGroup{
	"Process Routes",
	[]rest.Route{
		{
			"POST",
			"Start Process",
			"/process",
			startProcessHF,
		},
		{
			"GET",
			"Get Process",
			"/process/:pid",
			getProcessHF,
		},
		{
			"DELETE",
			"Kill Process",
			"/process/:pid",
			killProcessHF,
		},
		{
			"GET",
			"Get Process Logs",
			"/process/:pid/logs",
			getProcessLogsHF,
		},
		{
			"GET",
			"Get Processes",
			"/process",
			getProcessesHF,
		},
	},
}

func startProcessHF(w http.ResponseWriter, r *http.Request, p rest.Params) error {
	command := Command{}
	if err := restutil.ReadJson(r, &command); err != nil {
		return err
	}
	if err := checkCommand(&command); err != nil {
		return rest.BadRequest(err)
	}

	// If channel is provided then check whether it is ready to be
	// first process subscriber and use it if it is
	var subscriber *Subscriber
	channelId := r.URL.Query().Get("channel")
	if channelId != "" {
		channel, ok := rpc.GetChannel(channelId)
		if !ok {
			m := fmt.Sprintf("Channel with id '%s' doesn't exist. Process won't be started", channelId)
			return rest.NotFound(errors.New(m))
		}
		subscriber = &Subscriber{
			Id:      channelId,
			Mask:    parseTypes(r.URL.Query().Get("types")),
			Channel: channel.Events,
		}
	}

	pb := NewBuilder().Cmd(command)

	if subscriber != nil {
		pb.FirstSubscriber(*subscriber)
	}

	process, err := pb.Start()
	if err != nil {
		return err
	}
	return restutil.WriteJson(w, process)
}

func getProcessHF(w http.ResponseWriter, r *http.Request, p rest.Params) error {
	pid, err := parsePid(p.Get("pid"))
	if err != nil {
		return rest.BadRequest(err)
	}

	process, err := Get(pid)
	if err != nil {
		return asHttpError(err)
	}
	return restutil.WriteJson(w, process)
}

func killProcessHF(w http.ResponseWriter, r *http.Request, p rest.Params) error {
	pid, err := parsePid(p.Get("pid"))
	if err != nil {
		return rest.BadRequest(err)
	}
	if err := Kill(pid); err != nil {
		return asHttpError(err)
	}
	return nil
}

func getProcessLogsHF(w http.ResponseWriter, r *http.Request, p rest.Params) error {
	pid, err := parsePid(p.Get("pid"))
	if err != nil {
		return rest.BadRequest(err)
	}

	// Parse 'from', if 'from' is not specified then read all the logs from the start
	// if 'from' format is different from the DATE_TIME_FORMAT then return 400
	from, err := parseTime(r.URL.Query().Get("from"), time.Time{})
	if err != nil {
		return rest.BadRequest(errors.New("Bad format of 'from', " + err.Error()))
	}

	// Parse 'till', if 'till' is not specified then 'now' is used for it
	// if 'till' format is different from the DATE_TIME_FORMAT then return 400
	till, err := parseTime(r.URL.Query().Get("till"), time.Now())
	if err != nil {
		return rest.BadRequest(errors.New("Bad format of 'till', " + err.Error()))
	}

	logs, err := ReadLogs(pid, from, till)
	if err != nil {
		return asHttpError(err)
	}

	// limit logs from the latest to the earliest
	// limit - how many the latest logs will be present
	// skip - how many log lines should be skipped from the end
	limit := restutil.IntQueryParam(r, "limit", DefaultLogsPerPageLimit)
	skip := restutil.IntQueryParam(r, "skip", 0)
	if limit < 1 {
		return rest.BadRequest(errors.New("Required 'limit' to be > 0"))
	}
	if skip < 0 {
		return rest.BadRequest(errors.New("Required 'skip' to be >= 0"))
	}
	len := len(logs)
	fromIdx := int(math.Max(float64(len-limit-skip), 0))
	toIdx := len - int(math.Min(float64(skip), float64(len)))

	// Respond with an appropriate logs format, default json
	format := r.URL.Query().Get("format")
	switch strings.ToLower(format) {
	case "text":
		for _, item := range logs[fromIdx:toIdx] {
			line := fmt.Sprintf("[%s] %s \t %s\n", item.Kind, item.Time.Format(DateTimeFormat), item.Text)
			io.WriteString(w, line)
		}
	default:
		return restutil.WriteJson(w, logs[fromIdx:toIdx])
	}
	return nil
}

func getProcessesHF(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
	all, err := strconv.ParseBool(r.URL.Query().Get("all"))
	if err != nil {
		all = false
	}
	return restutil.WriteJson(w, GetProcesses(all))
}

func asHttpError(err error) error {
	if npErr, ok := err.(*NoProcessError); ok {
		return rest.NotFound(npErr.error)
	}
	return err
}
