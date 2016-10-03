package process

type ProcessBuilder struct {
	command          Command
	beforeEventsHook func(p MachineProcess)
	firstSubscriber  *Subscriber
}

func NewBuilder() *ProcessBuilder {
	return &ProcessBuilder{}
}

func (pb *ProcessBuilder) Cmd(command Command) *ProcessBuilder {
	pb.command = command
	return pb
}

func (pb *ProcessBuilder) CmdLine(cmdLine string) *ProcessBuilder {
	pb.command.CommandLine = cmdLine
	return pb
}

func (pb *ProcessBuilder) CmdType(cmdType string) *ProcessBuilder {
	pb.command.Type = cmdType
	return pb
}

func (pb *ProcessBuilder) CmdName(cmdName string) *ProcessBuilder {
	pb.command.Name = cmdName
	return pb
}

func (pb *ProcessBuilder) BeforeEventsHook(hook func(p MachineProcess)) *ProcessBuilder {
	pb.beforeEventsHook = hook
	return pb
}

func (pb *ProcessBuilder) FirstSubscriber(subscriber Subscriber) *ProcessBuilder {
	pb.firstSubscriber = &subscriber
	return pb
}

func (pb *ProcessBuilder) Build() MachineProcess {
	p := MachineProcess{
		Name:        pb.command.Name,
		CommandLine: pb.command.CommandLine,
		Type:        pb.command.Type,
		beforeEventsHook: pb.beforeEventsHook,
	}
	if pb.firstSubscriber != nil {
		p.subs = []*Subscriber{pb.firstSubscriber}
	}
	return p
}

func (pb *ProcessBuilder) Start() (MachineProcess, error) {
	return Start(pb.Build())
}
