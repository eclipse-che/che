package term

import (
	"bytes"
	"encoding/json"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
	"github.com/eclipse/che/agents/go-agents/core/common"
	"github.com/eclipse/che/agents/go-agents/core/rest"
)

// WebSocketMessage represents message sent over websocket connection
type WebSocketMessage struct {
	Type string          `json:"type"`
	Data json.RawMessage `json:"data"`
}

var (
	upgrader = websocket.Upgrader{
		CheckOrigin: func(r *http.Request) bool {
			return true
		},
	}

	// Cmd is command used to start new shell
	Cmd string

	// PingPeriod defines period of WS pings
	PingPeriod = 60 * time.Second

	// HTTPRoutes provides http routes that should be handled to use terminal service
	HTTPRoutes = rest.RoutesGroup{
		Name: "Terminal routes",
		Items: []rest.Route{
			{
				Method:     "GET",
				Name:       "Connect to pty(websocket)",
				Path:       "/pty",
				HandleFunc: ConnectToPtyHF,
			},
		},
	}
)

// ConnectToPtyHF provides communication with TTY over websocket
func ConnectToPtyHF(w http.ResponseWriter, r *http.Request, _ rest.Params) error {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		// upgrader writes http error into response, so no need to process error
		return nil
	}

	wp, err := startPty(Cmd)
	if err != nil {
		sendInternalError(conn, "Failed to start command: "+err.Error())
		return nil
	}

	reader := ioutil.NopCloser(wp.ptyFile)
	finalizer := newFinalizer(reader, conn, wp.ptyFile)

	log.Println("Start new terminal.")
	defer log.Println("Terminal process stopped.")
	defer finalizer.close()

	// send ping messages
	go setupWSPinging(conn, finalizer)
	//read output from terminal
	go sendPtyOutputToConnection(conn, reader, finalizer)
	//write input to terminal
	go sendConnectionInputToPty(conn, wp, finalizer)

	waitPTY(wp)

	return nil
}

// read from the web socket, copying to the pty master
// messages are expected to be text and base64 encoded
func sendConnectionInputToPty(conn *websocket.Conn, wp *wsPty, finalizer *readWriteRoutingFinalizer) {
	defer finalizer.closeReader()

	for {
		mt, payload, err := conn.ReadMessage()
		if err != nil {
			if !isNormalWSError(err) {
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
			if errMsg := wp.handleMessage(msg); errMsg != nil {
				log.Print(errMsg.Error())
				return
			}

		default:
			log.Printf("Invalid websocket message type %d\n", mt)
			return
		}
	}
}

// copy everything from the pty master to the websocket
// using base64 encoding for now due to limitations in term.js
func sendPtyOutputToConnection(conn *websocket.Conn, reader io.Reader, finalizer *readWriteRoutingFinalizer) {
	defer finalizer.closeConn()

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

func setupWSPinging(conn *websocket.Conn, finalizer *readWriteRoutingFinalizer) {
	ticker := time.NewTicker(PingPeriod)
	defer ticker.Stop()
	// send ping messages by sheduler
	for range ticker.C {
		if err := writeWSMessageToSocket(conn, websocket.PingMessage, []byte{}, finalizer); err != nil {
			log.Printf("Error occurs on sending ping message to websocket. %v", err)
			return
		}
	}
}

func waitPTY(wp *wsPty) {
	// ignore SIGHUP(hang up) error it's a normal signal to close terminal
	if err := wp.cmd.Wait(); err != nil && err.Error() != "signal: hangup" {
		log.Printf("Failed to stop process, due to occurred error '%s'", err.Error())
	}
}

func sendInternalError(conn *websocket.Conn, err string) {
	log.Println(err)
	common.LogError(conn.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseInternalServerErr, err)))
	common.LogError(conn.Close())
}
