// moved from https://github.com/eclipse/che-lib/tree/master/websocket-terminal
package term

/*
 * websocket/pty proxy server:
 * This program wires a websocket to a pty master.
 *
 * Usage:
 * go build -o ws-pty-proxy server.go
 * ./websocket-terminal -cmd /bin/bash -addr :9000 -static $HOME/src/websocket-terminal
 * ./websocket-terminal -cmd /bin/bash -- -i
 *
 * TODO:
 *  * make more things configurable
 *  * switch back to binary encoding after fixing term.js (see index.html)
 *  * make errors return proper codes to the web client
 *
 * Copyright 2014 Al Tobey tobert@gmail.com
 * MIT License, see the LICENSE file
 */

import (
	"bufio"
	"bytes"
	"encoding/json"
	"errors"
	"flag"
	"fmt"
	"github.com/eclipse/che-lib/pty"
	"github.com/eclipse/che-lib/websocket"
	"github.com/eclipse/che/exec-agent/rest"
	"io"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"os"
	"os/exec"
	"sync"
	"unicode/utf8"
)

type wsPty struct {
	Cmd     *exec.Cmd // pty builds on os.exec
	PtyFile *os.File  // a pty is simply an os.File
}

type WebSocketMessage struct {
	Type string          `json:"type"`
	Data json.RawMessage `json:"data"`
}

type ReadWriteRoutingFinalizer struct {
	*sync.Mutex
	readDone  bool
	writeDone bool
}

var (
	upgrader = websocket.Upgrader{
		ReadBufferSize:  1,
		WriteBufferSize: 1,
		CheckOrigin: func(r *http.Request) bool {
			return true
		},
	}

	Cmd string

	HttpRoutes = rest.RoutesGroup{
		"Terminal routes",
		[]rest.Route{
			{
				"GET",
				"Connect to pty(webscoket)",
				"/pty",
				ConnectToPtyHF,
			},
		},
	}
)

func StartPty() (*wsPty, error) {
	// TODO consider whether these args are needed at all
	cmd := exec.Command(Cmd, flag.Args()...)
	cmd.Env = append(os.Environ(), "TERM=xterm")

	file, err := pty.Start(cmd)
	if err != nil {
		return nil, err
	}

	//Set the size of the pty
	pty.Setsize(file, 60, 200)

	return &wsPty{
		PtyFile: file,
		Cmd:     cmd,
	}, nil
}

func isNormalWsError(err error) bool {
	closeErr, ok := err.(*websocket.CloseError)
	if ok && (closeErr.Code == websocket.CloseGoingAway || closeErr.Code == websocket.CloseNormalClosure) {
		return true
	}
	_, ok = err.(*net.OpError)
	return ok
}

func isNormalPtyError(err error) bool {
	if err == io.EOF {
		return true
	}
	pathErr, ok := err.(*os.PathError)
	return ok &&
		pathErr.Op == "read" &&
		pathErr.Path == "/dev/ptmx" &&
		pathErr.Err.Error() == "input/output error"
}

// read from the web socket, copying to the pty master
// messages are expected to be text and base64 encoded
func sendConnectionInputToPty(conn *websocket.Conn, reader io.ReadCloser, f *os.File, finalizer *ReadWriteRoutingFinalizer) {
	defer closeReader(reader, f, finalizer)

	for {
		mt, payload, err := conn.ReadMessage()
		if err != nil {
			if !isNormalWsError(err) {
				log.Printf("conn.ReadMessage failed: %s\n", err)
			}
			return
		}
		switch mt {
		case websocket.BinaryMessage:
			log.Printf("Ignoring binary message: %q\n", payload)
		case websocket.TextMessage:
			var msg WebSocketMessage
			if err := json.Unmarshal(payload, &msg); err != nil {
				log.Printf("Invalid message %s\n", err)
				continue
			}
			if errMsg := handleMessage(msg, f); errMsg != nil {
				log.Printf(errMsg.Error())
				return
			}

		default:
			log.Printf("Invalid websocket message type %d\n", mt)
			return
		}
	}
}

func handleMessage(msg WebSocketMessage, ptyFile *os.File) error {
	switch msg.Type {
	case "resize":
		var size []float64
		if err := json.Unmarshal(msg.Data, &size); err != nil {
			log.Printf("Invalid resize message: %s\n", err)
		} else {
			pty.Setsize(ptyFile, uint16(size[1]), uint16(size[0]))
			Activity.Notify()
		}

	case "data":
		var dat string
		if err := json.Unmarshal(msg.Data, &dat); err != nil {
			log.Printf("Invalid data message %s\n", err)
		} else {
			ptyFile.Write([]byte(dat))
			Activity.Notify()
		}

	default:
		return errors.New("Invalid field message type: " + msg.Type + "\n")
	}
	return nil
}

//read byte array as Unicode code points (rune in go)
func normalizeBuffer(normalizedBuf *bytes.Buffer, buf []byte, n int) (int, error) {
	bufferBytes := normalizedBuf.Bytes()
	runeReader := bufio.NewReader(bytes.NewReader(append(bufferBytes[:], buf[:n]...)))
	normalizedBuf.Reset()
	i := 0
	for i < n {
		char, charLen, err := runeReader.ReadRune()
		if err != nil {
			return i, err
		}
		if char == utf8.RuneError {
			runeReader.UnreadRune()
			return i, nil
		}
		i += charLen
		if _, err := normalizedBuf.WriteRune(char); err != nil {
			return i, err
		}
	}
	return i, nil
}

// copy everything from the pty master to the websocket
// using base64 encoding for now due to limitations in term.js
func sendPtyOutputToConnection(conn *websocket.Conn, reader io.ReadCloser, finalizer *ReadWriteRoutingFinalizer) {
	defer closeConn(conn, finalizer)

	buf := make([]byte, 8192)
	var buffer bytes.Buffer
	// TODO: more graceful exit on socket close / process exit
	for {
		n, err := reader.Read(buf)
		if err != nil {
			if !isNormalPtyError(err) {
				log.Printf("Failed to read from pty: %s", err)
			}
			return
		}
		i, err := normalizeBuffer(&buffer, buf, n)
		if err != nil {
			log.Printf("Cound't normalize byte buffer to UTF-8 sequence, due to an error: %s", err.Error())
			return
		}
		if err = conn.WriteMessage(websocket.TextMessage, buffer.Bytes()); err != nil {
			log.Printf("Failed to send websocket message: %s, due to occurred error %s", string(buffer.Bytes()), err.Error())
			return
		}
		buffer.Reset()
		if i < n {
			buffer.Write(buf[i:n])
		}
	}
}

func ptyHandler(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Fatalf("Websocket upgrade failed: %s\n", err)
	}

	wp, err := StartPty()
	if err != nil {
		log.Fatalf("Failed to start command: %s\n", err)
		return
	}

	reader := ioutil.NopCloser(wp.PtyFile)
	finalizer := ReadWriteRoutingFinalizer{&sync.Mutex{}, false, false}

	defer waitAndClose(wp, &finalizer, conn, reader)

	//read output from terminal
	go sendPtyOutputToConnection(conn, reader, &finalizer)
	//write input to terminal
	go sendConnectionInputToPty(conn, reader, wp.PtyFile, &finalizer)

	fmt.Println("New terminal succesfully initialized.")
}

func waitAndClose(wp *wsPty, finalizer *ReadWriteRoutingFinalizer, conn *websocket.Conn, reader io.ReadCloser) {
	if err := wp.Cmd.Wait(); err != nil {
		log.Printf("Failed to stop process, due to occurred error '%s'", err.Error())
	}

	wp.PtyFile.Close()

	closeConn(conn, finalizer)
	closeReader(reader, wp.PtyFile, finalizer)

	fmt.Println("Terminal process completed.")
}

func closeReader(reader io.ReadCloser, file *os.File, finalizer *ReadWriteRoutingFinalizer) {
	defer finalizer.Unlock()

	finalizer.Lock()
	if !finalizer.readDone {
		closeReaderErr := reader.Close()
		if closeReaderErr != nil {
			log.Printf("Failed to close pty file reader '%s'" + closeReaderErr.Error())
		}
		//hack to prevent suspend reader on the operation read when file has been already closed.
		file.Write([]byte("0"))
		finalizer.readDone = true
		fmt.Println("Terminal reader closed.")
	}

}

func closeConn(conn *websocket.Conn, finalizer *ReadWriteRoutingFinalizer) {
	defer finalizer.Unlock()

	finalizer.Lock()
	if !finalizer.writeDone {
		conn.Close()
		finalizer.writeDone = true
		fmt.Println("Terminal writer closed.")
	}
}

func ConnectToPtyHF(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
	ptyHandler(w, r)
	return nil
}
