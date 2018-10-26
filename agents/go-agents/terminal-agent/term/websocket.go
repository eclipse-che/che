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

package term

import (
	"log"
	"net"

	"github.com/gorilla/websocket"
)

func isNormalWSError(err error) bool {
	closeErr, ok := err.(*websocket.CloseError)
	if ok && (closeErr.Code == websocket.CloseGoingAway || closeErr.Code == websocket.CloseNormalClosure) {
		return true
	}
	_, ok = err.(*net.OpError)
	return ok
}

// writeToSocket writes message of type TextMessage into websocket connection
func writeToSocket(conn *websocket.Conn, bytes []byte, finalizer *readWriteRoutingFinalizer) error {
	return writeWSMessageToSocket(conn, websocket.TextMessage, bytes, finalizer)
}

// we write message to websocket with help mutex finalizer to prevent send message after "close  connection" message.
func writeWSMessageToSocket(conn *websocket.Conn, messageType int, bytes []byte, finalizer *readWriteRoutingFinalizer) error {
	defer finalizer.Unlock()

	finalizer.Lock()
	if err := conn.WriteMessage(messageType, bytes); err != nil {
		log.Printf("Failed to send websocket message: %s, due to occurred error %s", string(bytes), err.Error())
		return err
	}
	return nil
}
