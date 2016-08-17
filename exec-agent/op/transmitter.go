package op

// A Transmitter interface is used for sending
// results of the operation executions to the channel.
type Transmitter interface {

	// The id of the channel to which the message will be send
	Channel() Channel

	// Wraps the given message with an 'op.Result' and sends it to the client.
	Send(message interface{})

	// Wraps the given error with an 'op.Result' and sends it to the client.
	SendError(err Error)
}

type defaultTransmitter struct {
	id      interface{}
	channel Channel
}

func (t *defaultTransmitter) Channel() Channel { return t.channel }

func (t *defaultTransmitter) Send(message interface{}) {
	t.channel.output <- &Result{
		Id:   t.id,
		Body: message,
	}
}

func (t *defaultTransmitter) SendError(err Error) {
	t.channel.output <- &Result{
		Id:    t.id,
		Error: &err,
	}
}
