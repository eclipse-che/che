package process

import (
	"errors"
	"fmt"
	"github.com/eclipse/che/exec-agent/op"
	"github.com/eclipse/che/exec-agent/rest"
	"github.com/eclipse/che/exec-agent/rest/restutil"
	"github.com/gorilla/mux"
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
			"/process/{pid}",
			getProcessHF,
		},
		{
			"DELETE",
			"Kill Process",
			"/process/{pid}",
			killProcessHF,
		},
		{
			"GET",
			"Get Process Logs",
			"/process/{pid}/logs",
			getProcessLogsHF,
		},
		{
			"GET",
			"Get Processes",
			"/process",
			getProcessesHF,
		},
		{
			"DELETE",
			"Unsubscribe from Process Events",
			"/process/{pid}/events/{channel}",
			unsubscribeHF,
		},
		{
			"POST",
			"Subscribe to Process Events",
			"/process/{pid}/events/{channel}",
			subscribeHF,
		},
		{
			"PUT",
			"Update Process Events Subscriber",
			"/process/{pid}/events/{channel}",
			updateSubscriberHF,
		},
	},
}

func startProcessHF(w http.ResponseWriter, r *http.Request) error {
	command := Command{}
	restutil.ReadJson(r, &command)
	if err := checkCommand(&command); err != nil {
		return rest.BadRequest(err)
	}

	// If channel is provided then check whether it is ready to be
	// first process subscriber and use it if it is
	var subscriber *Subscriber
	channelId := r.URL.Query().Get("channel")
	if channelId != "" {
		channel, ok := op.GetChannel(channelId)
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

	process := NewProcess(command)

	if subscriber != nil {
		process.AddSubscriber(subscriber)
	}

	err := process.Start()
	if err != nil {
		return err
	}
	return restutil.WriteJson(w, process)
}

func getProcessHF(w http.ResponseWriter, r *http.Request) error {
	pid, err := parsePid(mux.Vars(r)["pid"])
	if err != nil {
		return rest.BadRequest(err)
	}

	process, ok := Get(pid)

	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("No process with id '%d'", pid)))
	}
	return restutil.WriteJson(w, process)
}

func killProcessHF(w http.ResponseWriter, r *http.Request) error {
	pid, err := parsePid(mux.Vars(r)["pid"])
	if err != nil {
		return rest.BadRequest(err)
	}
	p, ok := Get(pid)
	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("No process with id '%d'", pid)))
	}
	if err := p.Kill(); err != nil {
		return err
	}
	return nil
}

func getProcessLogsHF(w http.ResponseWriter, r *http.Request) error {
	pid, err := parsePid(mux.Vars(r)["pid"])
	if err != nil {
		return rest.BadRequest(err)
	}
	p, ok := Get(pid)
	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("No process with id '%d'", pid)))
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

	logs, err := p.ReadLogs(from, till)
	if err != nil {
		return err
	}

	// limit logs from the latest to the earliest
	// limit - how many the latest logs will be present
	// skip - how many log lines should be skipped from the end
	limit := restutil.IntQueryParam(r, "limit", DefaultLogsLimit)
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
			line := fmt.Sprintf("[%s] %s \t %s", item.Kind, item.Time.Format(DateTimeFormat), item.Text)
			io.WriteString(w, line)
		}
	default:
		return restutil.WriteJson(w, logs[fromIdx:toIdx])
	}
	return nil
}

func getProcessesHF(w http.ResponseWriter, r *http.Request) error {
	all, err := strconv.ParseBool(r.URL.Query().Get("all"))
	if err != nil {
		all = false
	}
	return restutil.WriteJson(w, GetProcesses(all))
}

func unsubscribeHF(w http.ResponseWriter, r *http.Request) error {
	vars := mux.Vars(r)
	pid, err := parsePid(vars["pid"])
	if err != nil {
		return rest.BadRequest(err)
	}

	// Getting process
	p, ok := Get(pid)
	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("No process with id '%d'", pid)))
	}

	channelId := vars["channel"]

	// Getting channel
	channel, ok := op.GetChannel(channelId)
	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("Channel with id '%s' doesn't exist", channelId)))
	}

	p.RemoveSubscriber(channel.Id)
	return nil
}

func subscribeHF(w http.ResponseWriter, r *http.Request) error {
	vars := mux.Vars(r)
	pid, err := parsePid(vars["pid"])
	if err != nil {
		return rest.BadRequest(err)
	}

	// Getting process
	p, ok := Get(pid)
	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("No process with id '%d'", pid)))
	}

	channelId := vars["channel"]

	// Getting channel
	channel, ok := op.GetChannel(channelId)
	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("Channel with id '%s' doesn't exist", channelId)))
	}

	subscriber := &Subscriber{Mask: parseTypes(r.URL.Query().Get("types")), Channel: channel.Events}

	// Check whether subscriber should see previous process logs
	afterStr := r.URL.Query().Get("after")
	if afterStr == "" {
		return p.AddSubscriber(subscriber)
	}
	after, err := time.Parse(DateTimeFormat, afterStr)
	if err != nil {
		return rest.BadRequest(errors.New("Bad format of 'after', " + err.Error()))
	}
	return p.RestoreSubscriber(subscriber, after)

}

func updateSubscriberHF(w http.ResponseWriter, r *http.Request) error {
	vars := mux.Vars(r)
	pid, err := parsePid(vars["pid"])
	if err != nil {
		return rest.BadRequest(err)
	}

	// Getting process
	p, ok := Get(pid)
	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("No process with id '%d'", pid)))
	}

	channelId := vars["channel"]

	// Getting channel
	channel, ok := op.GetChannel(channelId)
	if !ok {
		return rest.NotFound(errors.New(fmt.Sprintf("Channel with id '%s' doesn't exist", channelId)))
	}

	// Parsing mask from the level e.g. events?types=stdout,stderr
	types := r.URL.Query().Get("types")
	if types == "" {
		return rest.BadRequest(errors.New("'types' parameter required"))
	}
	p.UpdateSubscriber(channel.Id, maskFromTypes(types))
	return nil
}
