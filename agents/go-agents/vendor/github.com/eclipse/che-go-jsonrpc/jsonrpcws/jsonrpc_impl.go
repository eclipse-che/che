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

// Package jsonrpcws provides implementation of jsonrpc.NativeConn based on websocket.
//
// The example:
//
// Client:
//	conn, err := jsonrpcws.Dial("ws://host:port/path", token123456798)
//	if err != nil {
//      	panic(err)
//      }
// 	tunnel := jsonrpc.NewTunnel(conn, jsonrpc.DefaultRouter)
//	tunnel.Go()
//	tunnel.SayHello()
//
// Server:
//	conn, err := jsonrpcws.Upgrade(w, r)
//	if err != nil {
//      	panic(err)
//      }
//	tunnel := jsonrpc.NewTunnel(conn, jsonrpc.DefaultRouter)
//	tunnel.Go()
//	tunnel.SayHello()
package jsonrpcws

import (
	"net/http"

	"github.com/gorilla/websocket"
	"github.com/eclipse/che-go-jsonrpc"
)

var (
	// default upgrader that is used for upgrading http connection to WebSocket
	// may be changed by client if custom settings are needed
	DefaultUpgrader = &websocket.Upgrader{
		ReadBufferSize:  1024,
		WriteBufferSize: 1024,
		CheckOrigin: func(r *http.Request) bool {
			return true
		},
	}

	// default dialer that is used for WebSocket connection establishing
	// may be changed by client if custom settings are needed
    DefaultDialer = websocket.DefaultDialer
)

// Dial establishes a new client WebSocket connection.
// If argument 'token' is empty authentication won't be used,
// otherwise authorization token will be added.
func Dial(url string, token string) (*NativeConnAdapter, error) {
	var headers http.Header
	if token != "" {
		headers = make(map[string][]string)
		headers.Add("Authorization", token)
	}
	conn, _, err := DefaultDialer.Dial(url, headers)
	if err != nil {
		return nil, err
	}
	return &NativeConnAdapter{conn: conn}, nil
}

// Upgrade upgrades http connection to WebSocket connection.
func Upgrade(w http.ResponseWriter, r *http.Request) (*NativeConnAdapter, error) {
	conn, err := DefaultUpgrader.Upgrade(w, r, nil)
	if err != nil {
		return nil, err
	}
	return &NativeConnAdapter{RequestURI: r.RequestURI, conn: conn}, nil
}

// NativeConnAdapter adapts WebSocket connection to jsonrpc.NativeConn.
type NativeConnAdapter struct {

	// RequestURI is http.Request URI which is set on connection Upgrade.
	RequestURI string

	// A real websocket connection.
	conn *websocket.Conn
}

// Write writes text message to the WebSocket connection.
func (adapter *NativeConnAdapter) Write(data []byte) error {
	return adapter.conn.WriteMessage(websocket.TextMessage, data)
}

// Next reads next text message from the WebSocket connection.
func (adapter *NativeConnAdapter) Next() ([]byte, error) {
	for {
		mType, data, err := adapter.conn.ReadMessage()
		if err != nil {
			if closeErr, ok := err.(*websocket.CloseError); ok {
				return nil, jsonrpc.NewCloseError(closeErr)
			}
			return nil, err
		}
		if mType == websocket.TextMessage {
			return data, nil
		}
	}
}

// Close closes this connection.
func (adapter *NativeConnAdapter) Close() error {
	err := adapter.conn.Close()
	if closeErr, ok := err.(*websocket.CloseError); ok && isNormallyClosed(closeErr.Code) {
		return nil
	}
	return err
}

func isNormallyClosed(code int) bool {
	return code == websocket.CloseGoingAway ||
		code == websocket.CloseNormalClosure ||
		code == websocket.CloseNoStatusReceived
}
