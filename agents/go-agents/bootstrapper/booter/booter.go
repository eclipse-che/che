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

// Package booter (short for bootstrapper) provides facilities for
// executing installations and streaming statuses/logs related to installation process.
// Statuses and logs are defined by bootstrapper spec
// see https://github.com/eclipse/che/issues/4096#issuecomment-283067971
package booter

import (
	"errors"
	"log"
	"sync"
	"time"

	"github.com/eclipse/che-go-jsonrpc/event"
	"github.com/eclipse/che-go-jsonrpc"
	"github.com/eclipse/che/agents/go-agents/core/process"
)

const (
	installerCmdType = "installer"
)

var (
	installers                  []Installer
	runtimeID                   RuntimeID
	machineName                 string
	installerTimeout            time.Duration
	checkPeriod                 time.Duration
	logsEndpointReconnectPeriod time.Duration

	bus = event.NewBus()
)

// Connector encloses implementation specific jsonrpc connection establishment.
type Connector interface {
	Connect() (*jsonrpc.Tunnel, error)
}

// Init sets initializes bootstrapper configuration.
func Init(id RuntimeID, mName string, instTimeoutSec, checkPeriodSec, logsEndpointReconnectPeriodSec int) {
	runtimeID = id
	machineName = mName
	installerTimeout = time.Second * time.Duration(instTimeoutSec)
	checkPeriod = time.Second * time.Duration(checkPeriodSec)
	logsEndpointReconnectPeriod = time.Second * time.Duration(logsEndpointReconnectPeriodSec)
}

// AddAll adds batch of installers to the installation sequence.
func AddAll(newInstallers []Installer) {
	installers = append(installers, newInstallers...)
}

// PushLogs sets given tunnel as consumer of installer logs.
// Connector is used to reconnect to jsonrpc endpoint if
// established connection behind given tunnel was lost.
func PushLogs(tun *jsonrpc.Tunnel, connector Connector) {
	bus.Sub(&tunnelBroadcaster{
		tunnel:          tun,
		connector:       connector,
		reconnectPeriod: logsEndpointReconnectPeriod,
		reconnectOnce:   &sync.Once{},
	}, InstallerLogEventType)
}

// PushStatuses sets given tunnel as consumer of installer/bootstrapper statuses.
func PushStatuses(tun *jsonrpc.Tunnel) {
	bus.SubAny(&tunnelBroadcaster{tunnel: tun}, InstallerStatusChangedEventType, StatusChangedEventType)
}

// Start starts installation.
// If there is at least one installer which defines server, this func
// will hold until all the processes which started the server die.
// If there is no installer which provides server, this func exits after
// all the installation are completed.
// In both cases if any error occurs during installation,
// start exits returning that error.
// If any different from nil error value returned, bootstrapping should be considered as failed.
func Start() error {
	if len(installers) == 0 {
		return errors.New("No installers added, nothing to start")
	}

	printPlan()

	log.Print("Starting installations")
	pubStarting()
	for _, installer := range installers {
		pubStartingInstallation(installer.ID)

		log.Printf("Installing '%s'", installer.ID)
		if err := installOne(installer); err != nil {
			log.Printf("Installation of '%s' failed", installer.ID)
			pubInstallationFailed(installer.ID, err.Error())
			pubBootstrappingFailed(err.Error())
			closeConsumers()
			killProcesses()
			return err
		}

		log.Printf("Installation of '%s' successfully finished", installer.ID)
		pubInstallationCompleted(installer)
	}
	log.Printf("All installations successfully finished")
	pubBootstrappingDone()

	err := waitStartedProcessesDie()
	closeConsumers()
	return err
}

func installOne(installer Installer) error {
	var inst installation
	if installer.HasServers() {
		inst = &serverInst{installer, checkPeriod, installerTimeout}
	} else {
		inst = &scriptInst{installer, installerTimeout}
	}
	return inst.execute()
}

func broadcastLogs(installer string, event process.Event) {
	if outEvent, ok := event.(*process.OutputEvent); ok {
		var stream string
		if event.Type() == process.StderrEventType {
			stream = StderrStream
		} else {
			stream = StdoutStream
		}
		bus.Pub(&InstallerLogEvent{
			Stream:    stream,
			Text:      outEvent.Text,
			Installer: installer,
			MachineEvent: MachineEvent{
				MachineName: machineName,
				RuntimeID:   runtimeID,
				Time:        time.Now(),
			},
		})
	}
}

func closeConsumers() {
	for _, candidates := range bus.Clear() {
		for _, candidate := range candidates {
			if broadcaster, ok := candidate.(*tunnelBroadcaster); ok {
				broadcaster.Close()
			}
		}
	}
}

func killProcesses() {
	for _, p := range aliveStartedProcesses() {
		if err := process.Kill(p.Pid); err != nil {
			log.Print(err)
		}
	}
}

func pubBootstrappingFailed(err string) {
	bus.Pub(&StatusChangedEvent{
		Status: StatusFailed,
		Error:  err,
		MachineEvent: MachineEvent{
			MachineName: machineName,
			RuntimeID:   runtimeID,
			Time:        time.Now(),
		},
	})
}

func pubBootstrappingDone() {
	bus.Pub(&StatusChangedEvent{
		Status: StatusDone,
		MachineEvent: MachineEvent{
			MachineName: machineName,
			RuntimeID:   runtimeID,
			Time:        time.Now(),
		},
	})
}

func pubStarting() {
	bus.Pub(&StatusChangedEvent{
		Status: StatusStarting,
		MachineEvent: MachineEvent{
			MachineName: machineName,
			RuntimeID:   runtimeID,
			Time:        time.Now(),
		},
	})
}

func pubStartingInstallation(installer string) {
	bus.Pub(&InstallerStatusChangedEvent{
		Status:    InstallerStatusStarting,
		Installer: installer,
		MachineEvent: MachineEvent{
			MachineName: machineName,
			RuntimeID:   runtimeID,
			Time:        time.Now(),
		},
	})
}

func pubInstallationFailed(installer string, err string) {
	bus.Pub(&InstallerStatusChangedEvent{
		Status:    InstallerStatusFailed,
		Installer: installer,
		Error:     err,
		MachineEvent: MachineEvent{
			MachineName: machineName,
			RuntimeID:   runtimeID,
			Time:        time.Now(),
		},
	})
}

func pubInstallationCompleted(installer Installer) {
	var status string
	if installer.HasServers() {
		status = InstallerStatusRunning
	} else {
		status = InstallerStatusDone
	}
	bus.Pub(&InstallerStatusChangedEvent{
		Status:    status,
		Installer: installer.ID,
		MachineEvent: MachineEvent{
			MachineName: machineName,
			RuntimeID:   runtimeID,
			Time:        time.Now(),
		},
	})
}

// Wait until all the started processes by bootstrapper die.
// If all the installers do not provide servers - this method exits.
// Otherwise, if there is an installer which starts server, this func
// will hold until the processes which started servers are all dead.
func waitStartedProcessesDie() error {
	processes := aliveStartedProcesses()
	if len(processes) == 0 {
		return nil
	}

	wg := sync.WaitGroup{}
	wg.Add(len(processes))

	now := time.Now()
	for _, p := range processes {
		subscriber := process.Subscriber{
			ID:   "wait-" + p.Name,
			Mask: process.StatusBit,
			Consumer: process.EventConsumerFunc(func(e process.Event) {
				if e.Type() == process.DiedEventType {
					wg.Done()
				}
			}),
		}
		if err := process.RestoreSubscriber(p.Pid, subscriber, now); err != nil {
			log.Printf("Trying to wait process. Error restoring subscriber %s", err)
			wg.Done()
		}
	}

	wg.Wait()
	return nil
}

func aliveStartedProcesses() []process.MachineProcess {
	allProcesses := process.GetProcesses(false)
	result := make([]process.MachineProcess, 0, len(allProcesses))
	for _, p := range allProcesses {
		if p.Type == installerCmdType {
			result = append(result, p)
		}
	}
	return result
}

type tunnelBroadcaster struct {
	tunnel          *jsonrpc.Tunnel
	connector       Connector
	reconnectPeriod time.Duration
	reconnectOnce   *sync.Once
}

func (tb *tunnelBroadcaster) Accept(e event.E) {
	if err := tb.tunnel.Notify(e.Type(), e); err != nil {
		log.Printf("Trying to send event of type '%s' to closed tunnel '%s'", e.Type(), tb.tunnel.ID())
		if tb.connector != nil && tb.reconnectPeriod > 0 {
			// if multiple accepts are on this point
			tb.reconnectOnce.Do(func() { tb.goReconnect() })
		}
	}
}

func (tb *tunnelBroadcaster) IsDone() bool {
	return tb.tunnel.IsClosed()
}

func (tb *tunnelBroadcaster) Close() { tb.tunnel.Close() }

func (tb *tunnelBroadcaster) goReconnect() {
	go func() {
		time.Sleep(tb.reconnectPeriod)

		if tunnel, err := tb.connector.Connect(); err != nil {
			log.Printf("Reconnect to logs endpoint failed, next attempt in %ds", logsEndpointReconnectPeriod/time.Second)
			tb.goReconnect()
		} else {
			log.Printf("Successfully reconnected to logs endpoint")
			PushLogs(tunnel, tb.connector)
		}
	}()
}

func printPlan() {
	log.Print("Planning to install")
	for _, installer := range installers {
		log.Printf("- %s:%s - %s", installer.ID, installer.Version, installer.Description)
	}
}
