//
// Copyright (c) 2012-2018 Red Hat, Inc.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Red Hat, Inc. - initial API and implementation
//

package term

import (
	"log"
	"net"

	"github.com/eclipse/che-lib/websocket"
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
