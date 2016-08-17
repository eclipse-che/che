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
	"flag"
	"github.com/eclipse/che-lib/pty"
	"github.com/eclipse/che/exec-agent/rest"
	"github.com/gorilla/websocket"
	"io"
	"log"
	"net/http"
	"os"
	"os/exec"
	"regexp"
	"unicode/utf8"
	"github.com/eclipse/che/exec-agent/auth"
)

type wsPty struct {
	Cmd *exec.Cmd // pty builds on os.exec
	Pty *os.File  // a pty is simply an os.File
}

type route struct {
	pattern *regexp.Regexp
	handler http.Handler
}

var (
	cmdFlag string

	upgrader = websocket.Upgrader{
		ReadBufferSize:  1,
		WriteBufferSize: 1,
		CheckOrigin: func(r *http.Request) bool {
			return true
		},
	}

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

func init() {
	flag.StringVar(&cmdFlag, "cmd", "/bin/bash", "command to execute on slave side of the pty")
}

func (wp *wsPty) Start() {
	var err error
	args := flag.Args()
	wp.Cmd = exec.Command(cmdFlag, args...)
	env := os.Environ()
	env = append(env, "TERM=xterm")
	wp.Cmd.Env = env
	wp.Pty, err = pty.Start(wp.Cmd)
	if err != nil {
		log.Fatalf("Failed to start command: %s\n", err)
	}
	//Set the size of the pty
	pty.Setsize(wp.Pty, 60, 200)
}

func (wp *wsPty) Stop() {
	wp.Pty.Close()
	wp.Cmd.Wait()
}

func ptyHandler(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Fatalf("Websocket upgrade failed: %s\n", err)
	}
	defer conn.Close()

	wp := wsPty{}
	// TODO: check for errors, return 500 on fail
	wp.Start()

	// copy everything from the pty master to the websocket
	// using base64 encoding for now due to limitations in term.js
	go func() {
		buf := make([]byte, 8192)
		reader := bufio.NewReader(wp.Pty)
		var buffer bytes.Buffer
		// TODO: more graceful exit on socket close / process exit
		for {
			n, err := reader.Read(buf)

			if err != nil {
				log.Printf("Failed to read from pty master: %s", err)
				return
			}
			//read byte array as Unicode code points (rune in go)

			bufferBytes := buffer.Bytes()
			runeReader := bufio.NewReader(bytes.NewReader(append(bufferBytes[:], buf[:n]...)))
			buffer.Reset()
			i := 0
			for i < n {
				char, charLen, e := runeReader.ReadRune()
				if e != nil {
					log.Printf("Failed to read from pty master: %s", err)
					return
				}

				if char == utf8.RuneError {
					runeReader.UnreadRune()
					break
				}
				i += charLen
				buffer.WriteRune(char)
			}
			err = conn.WriteMessage(websocket.TextMessage, buffer.Bytes())
			if err != nil {
				log.Printf("Failed to send UTF8 char: %s", err)
				return
			}
			buffer.Reset()
			if i < n {
				buffer.Write(buf[i:n])
			}
		}
	}()

	type Message struct {
		Type string          `json:"type"`
		Data json.RawMessage `json:"data"`
	}

	// read from the web socket, copying to the pty master
	// messages are expected to be text and base64 encoded
	for {
		mt, payload, err := conn.ReadMessage()
		if err != nil {
			if err != io.EOF {
				log.Printf("conn.ReadMessage failed: %s\n", err)
				return
			}
		}
		var msg Message
		switch mt {
		case websocket.BinaryMessage:
			log.Printf("Ignoring binary message: %q\n", payload)
		case websocket.TextMessage:
			err := json.Unmarshal(payload, &msg)
			if err != nil {
				log.Printf("Invalid message %s\n", err)
				continue
			}
			switch msg.Type {
			case "resize":
				var size []float64
				err := json.Unmarshal(msg.Data, &size)
				if err != nil {
					log.Printf("Invalid resize message: %s\n", err)
				} else {
					pty.Setsize(wp.Pty, uint16(size[1]), uint16(size[0]))
					if ActivityTrackingEnabled {
						Activity.Notify()
					}
				}

			case "data":
				var dat string
				err := json.Unmarshal(msg.Data, &dat)
				if err != nil {
					log.Printf("Invalid data message %s\n", err)
				} else {
					wp.Pty.Write([]byte(dat))
					if ActivityTrackingEnabled {
						Activity.Notify()
					}
				}

			default:
				log.Printf("Invalid message type %d\n", mt)
				return
			}

		default:
			log.Printf("Invalid message type %d\n", mt)
			return
		}
	}
	wp.Stop()
}

func ConnectToPtyHF(w http.ResponseWriter, r *http.Request) error {
	if auth.Enabled {
		if err := auth.AuthenticateOnMaster(r); err != nil {
			return err
		}
	}
	ptyHandler(w, r)
	return nil
}
