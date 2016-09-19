package op

// Transmitter is used for sending
// results of the operation executions to the channel.
type Transmitter struct {

	// The id of the request behind this transmitter.
	id interface{}

	// The channel to which the message will be send.
	Channel Channel
}

// Wraps the given message with an 'op.Result' and sends it to the client.
func (t *Transmitter) Send(message interface{}) {
	t.Channel.output <- &Result{
		Id:   t.id,
		Body: message,
	}
}

// Wraps the given error with an 'op.Result' and sends it to the client.
func (t *Transmitter) SendError(err Error) {
	t.Channel.output <- &Result{
		Id:    t.id,
		Error: &err,
	}
}
