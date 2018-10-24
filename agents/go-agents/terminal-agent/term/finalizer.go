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
	"io"
	"log"
	"os"
	"sync"

	"github.com/gorilla/websocket"
)

// readWriteRoutingFinalizer helps to close connection in concurrent environment corectly
type readWriteRoutingFinalizer struct {
	sync.Mutex
	readDone   bool
	writeDone  bool
	fileClosed bool
	reader     io.ReadCloser
	conn       *websocket.Conn
	file       *os.File
}

func newFinalizer(reader io.ReadCloser, conn *websocket.Conn, file *os.File) *readWriteRoutingFinalizer {
	return &readWriteRoutingFinalizer{
		readDone:   false,
		writeDone:  false,
		fileClosed: false,
		reader:     reader,
		conn:       conn,
		file:       file,
	}
}

func (finalizer *readWriteRoutingFinalizer) close() {
	finalizer.closeFile()
	finalizer.closeConn()
	finalizer.closeReader()
}

// It was supposed that reconnection can be implemented later.
// So if connection lost PTY file is not closed to allow reconnection.
func (finalizer *readWriteRoutingFinalizer) closeConn() {
	defer finalizer.Unlock()

	finalizer.Lock()
	if !finalizer.writeDone {
		// to cleanly close websocket connection, a client should send a close
		// frame and wait for the server to close the connection.
		err := finalizer.conn.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))
		if err != nil {
			log.Printf("Failed to send websocket close message: '%s'", err.Error())
		}
		if err := finalizer.conn.Close(); err != nil {
			log.Printf("Close connection problem: '%s'", err.Error())
		}

		finalizer.writeDone = true
		log.Println("Terminal writer closed.")
	}
}

func (finalizer *readWriteRoutingFinalizer) closeFile() {
	defer finalizer.Unlock()

	finalizer.Lock()
	if !finalizer.fileClosed {
		if err := finalizer.file.Close(); err != nil {
			log.Printf("Failed to close pty file: '%s'", err.Error())
		}
		finalizer.fileClosed = true
		log.Println("Pty file closed.")
	}
}

func (finalizer *readWriteRoutingFinalizer) closeReader() {
	defer finalizer.Unlock()

	finalizer.Lock()
	if !finalizer.readDone {
		closeReaderErr := finalizer.reader.Close()
		if closeReaderErr != nil {
			log.Printf("Failed to close pty file reader: '%s'" + closeReaderErr.Error())
		}
		// hack to prevent suspend reader on the operation read when file has been already closed.
		_, _ = finalizer.file.Write([]byte{})
		finalizer.readDone = true
		log.Println("Terminal reader closed.")
	}

}
