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

package rpc

// Transmitter is used for sending
// results of the operation executions to the channel.
type Transmitter struct {

	// The id of the request behind this transmitter.
	id interface{}

	// The channel to which the message will be send.
	Channel Channel
}

// Wraps the given message with 'rpc.Result' and sends it to the client.
func (t *Transmitter) Send(message interface{}) {
	t.Channel.output <- &Response{
		Version: "2.0",
		Id:      t.id,
		Result:  message,
	}
}

// Wraps the given error with 'rpc.Result' and sends it to the client.
func (t *Transmitter) SendError(err Error) {
	t.Channel.output <- &Response{
		Version: "2.0",
		Id:      t.id,
		Error:   &err,
	}
}
