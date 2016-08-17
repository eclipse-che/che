// TODO add subscribed event types
package process

const (
	ProcessStartedEventType = "process_started"
	ProcessDiedEventType    = "process_died"
	StdoutEventType         = "stdout"
	StderrEventType         = "stderr"
)

type ProcessEventBody struct {
	Pid uint64 `json:"pid"`
}

type ProcessStatusEventBody struct {
	ProcessEventBody
	NativePid   int    `json:"nativePid"`
	Name        string `json:"name"`
	CommandLine string `json:"commandLine"`
}

type ProcessOutputEventBody struct {
	ProcessEventBody
	Text string `json:"text"`
}
