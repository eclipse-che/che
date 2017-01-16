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
	"github.com/eclipse/che/agents/exec-agent/rest"
	"io"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"os"
	"os/exec"
	"sync"
	"syscall"
	"unicode/utf8"
)

type wsPty struct {
	Cmd     *exec.Cmd // pty builds on os.exec
	PtyFile *os.File  // a pty is simply an os.File
}

//Stop terminal process and its child processes. In modern Unix systems terminal stops with help
// SIGHUP signal and we used such way too. SIGHUP signal used to send a signal to a process
// (or process group), it's signal meaning that pseudo or virtual terminal has been closed.
// Example: command is executed inside a terminal window and the terminal window is closed while
// the command process is still running.
// If the process receiving SIGHUP is a Unix shell, then as part of job control it will often intercept
// the signal and ensure that all stopped processes are continued before sending the signal to child
// processes (more precisely, process groups, represented internally be the shell as a "job"), which
// by default terminates them.
func (wp *wsPty) Close(finalizer *ReadWriteRoutingFinalizer) {
	closeFile(wp.PtyFile, finalizer)
	pid := wp.Cmd.Process.Pid

	if pgid, err := syscall.Getpgid(pid); err == nil {
		if err := syscall.Kill(-pgid, syscall.SIGHUP); err != nil {
			fmt.Errorf("Failed to SIGHUP terminal process by pgid: '%s'. Cause: '%s'", pgid, err)
		}
	}
	if err := syscall.Kill(pid, syscall.SIGHUP); err != nil {
		fmt.Errorf("Failed to SIGHUP terminal process by pid '%s'. Cause: '%s'", pid, err)
	}
}

type WebSocketMessage struct {
	Type string          `json:"type"`
	Data json.RawMessage `json:"data"`
}

type ReadWriteRoutingFinalizer struct {
	*sync.Mutex
	readDone   bool
	writeDone  bool
	fileClosed bool
}

var (
	upgrader = websocket.Upgrader{
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
func sendConnectionInputToPty(conn *websocket.Conn, reader io.ReadCloser, wp *wsPty, finalizer *ReadWriteRoutingFinalizer) {
	f := wp.PtyFile
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
			if msg.Type == "close" {
				wp.Close(finalizer)
				return
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
			log.Printf("Couldn't normalize byte buffer to UTF-8 sequence, due to an error: %s", err.Error())
			return
		}

		if err := writeToSocket(conn, buffer.Bytes(), finalizer); err != nil {
			return
		}

		buffer.Reset()
		if i < n {
			buffer.Write(buf[i:n])
		}
	}
}

//we write message to websocket with help mutex finalizer to prevent send message after "close  connection" message.
func writeToSocket(conn *websocket.Conn, bytes []byte, finalizer *ReadWriteRoutingFinalizer) error {
	defer finalizer.Unlock()

	finalizer.Lock()
	if err := conn.WriteMessage(websocket.TextMessage, bytes); err != nil {
		log.Printf("Failed to send websocket message: %s, due to occurred error %s", string(bytes), err.Error())
		return err
	}
	return nil
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
	finalizer := ReadWriteRoutingFinalizer{&sync.Mutex{}, false, false, false}

	defer waitAndClose(wp, &finalizer, conn, reader)

	//read output from terminal
	go sendPtyOutputToConnection(conn, reader, &finalizer)
	//write input to terminal
	go sendConnectionInputToPty(conn, reader, wp, &finalizer)

	fmt.Println("New terminal successfully initialized.")
}

func waitAndClose(wp *wsPty, finalizer *ReadWriteRoutingFinalizer, conn *websocket.Conn, reader io.ReadCloser) {
	//ignore GIGHUP(hang up) error it's a normal signal to close terminal
	if err := wp.Cmd.Wait(); err != nil && err.Error() != "signal: hangup" {
		log.Printf("Failed to stop process, due to occurred error '%s'", err.Error())
	}

	closeFile(wp.PtyFile, finalizer)
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
			log.Printf("Failed to close pty file reader: '%s'" + closeReaderErr.Error())
		}
		//hack to prevent suspend reader on the operation read when file has been already closed.
		file.Write([]byte{})
		finalizer.readDone = true
		fmt.Println("Terminal reader closed.")
	}

}

func closeConn(conn *websocket.Conn, finalizer *ReadWriteRoutingFinalizer) {
	defer finalizer.Unlock()

	finalizer.Lock()
	if !finalizer.writeDone {
		//to cleanly close websocket connection, a client should send a close
		//frame and wait for the server to close the connection.
		err := conn.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))
		if err != nil {
			log.Printf("Failed to send websocket close message: '%s'", err.Error())
		}
		if err := conn.Close(); err != nil {
			fmt.Printf("Close connection problem: '%s'", err.Error())
		}

		finalizer.writeDone = true
		fmt.Println("Terminal writer closed.")
	}
}

func closeFile(file *os.File, finalizer *ReadWriteRoutingFinalizer) {
	defer finalizer.Unlock()

	finalizer.Lock()
	if !finalizer.fileClosed {
		if err := file.Close(); err != nil {
			log.Printf("Failed to close pty file: '%s'", err.Error())
		}
		finalizer.fileClosed = true
		fmt.Println("Pty file closed.")
	}
}

func ConnectToPtyHF(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
	ptyHandler(w, r)
	return nil
}
