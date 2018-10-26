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
	"bufio"
	"bytes"
	"encoding/json"
	"errors"
	"io"
	"log"
	"os"
	"os/exec"
	"syscall"
	"unicode/utf8"

	"github.com/eclipse/che/agents/go-agents/core/activity"
	"github.com/kr/pty"
)

type wsPty struct {
	cmd     *exec.Cmd // pty builds on os.exec
	ptyFile *os.File  // a pty is simply an os.File
}

// startPty starts shell interpreter and returns wsPty that represents this terminal
func startPty(command string) (*wsPty, error) {
	cmd := exec.Command(command)
	cmd.Env = append(os.Environ(), "TERM=xterm")

	file, err := pty.Start(cmd)
	if err != nil {
		return nil, err
	}

	//Set the size of the pty
	if err := pty.Setsize(file, &pty.Winsize{Cols: 200, Rows: 60, X: 0, Y: 0}); err != nil {
		log.Printf("Error occurs on setting terminal size. %s", err)
	}

	return &wsPty{
		ptyFile: file,
		cmd:     cmd,
	}, nil
}

// Close stops terminal process and its child processes. In modern Unix systems terminal stops with help
// SIGHUP signal and we used such way too. SIGHUP signal used to send a signal to a process
// (or process group), it's signal meaning that pseudo or virtual terminal has been closed.
// Example: command is executed inside a terminal window and the terminal window is closed while
// the command process is still running.
// If the process receiving SIGHUP is a Unix shell, then as part of job control it will often intercept
// the signal and ensure that all stopped processes are continued before sending the signal to child
// processes (more precisely, process groups, represented internally be the shell as a "job"), which
// by default terminates them.
func (wp *wsPty) Close(finalizer *readWriteRoutingFinalizer) {
	finalizer.closeFile()
	pid := wp.cmd.Process.Pid
	if pgid, err := syscall.Getpgid(pid); err == nil {
		if err := syscall.Kill(-pgid, syscall.SIGHUP); err != nil {
			log.Printf("Failed to SIGHUP terminal process by pgid: '%d'. Cause: '%s'", pgid, err)
		}
	}
	if err := syscall.Kill(pid, syscall.SIGHUP); err != nil {
		log.Printf("Failed to SIGHUP terminal process by pid '%d'. Cause: '%s'", pid, err)
	}
}

func (wp *wsPty) handleMessage(msg WebSocketMessage) error {
	switch msg.Type {
	case "resize":
		var size []float64
		if err := json.Unmarshal(msg.Data, &size); err != nil {
			log.Printf("Invalid resize message: %s\n", err)
		} else {
			if err := pty.Setsize(wp.ptyFile, &pty.Winsize{Cols: uint16(size[0]), Rows: uint16(size[1]), X: 0, Y: 0}); err != nil {
				log.Printf("Error occurs on setting terminal size. %s", err)
			}
			activity.Tracker.Notify()
		}

	case "data":
		var dat string
		if err := json.Unmarshal(msg.Data, &dat); err != nil {
			log.Printf("Invalid data message %s\n", err)
		} else {
			if _, err := wp.ptyFile.Write([]byte(dat)); err != nil {
				log.Printf("Error occurs on writing data into terminal. %s", err)
			}
			activity.Tracker.Notify()
		}

	default:
		return errors.New("Invalid field message type: " + msg.Type + "\n")
	}
	return nil
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

// read byte array as Unicode code points (rune in go)
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
			if err := runeReader.UnreadRune(); err != nil {
				log.Print(err)
			}
			return i, nil
		}
		i += charLen
		if _, err := normalizedBuf.WriteRune(char); err != nil {
			return i, err
		}
	}
	return i, nil
}
