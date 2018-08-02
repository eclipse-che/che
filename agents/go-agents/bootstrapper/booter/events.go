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

package booter

import (
	"time"
)

const (
	// StatusChangedEventType defines type of StatusChangedEvent.
	StatusChangedEventType = "bootstrapper/statusChanged"

	// InstallerStatusChangedEventType defines type of InstallerStatusChangedEvent.
	InstallerStatusChangedEventType = "installer/statusChanged"

	// InstallerLogEventType defines type of InstallerLogEvent.
	InstallerLogEventType = "installer/log"
)

// Bootstrapper statuses.
const (
	// StatusStarting indicates an event which published before
	// any other bootstrapper event. It notifies clients that bootstrapper
	// is ready and soon will start installations.
	StatusStarting = "STARTING"

	// StatusDone published after all the installations successfully finished.
	// The opposite event would be of type StatusFailed.
	StatusDone = "DONE"

	// StatusFailed published if any error occurs during the installation.
	// The opposite event would be of type StatusDone.
	StatusFailed = "FAILED"
)

// Installer statuses.
const (
	// InstallerStatusStarting published each time when bootstrapper starts the installer.
	InstallerStatusStarting = "STARTING"

	// InstallerStatusDone published each time when installation successfully finishes
	// for the installer which does not define server.
	InstallerStatusDone = "DONE"

	// InstallerStatusRunning published each time installation successfully finished
	// for the installer which defines server and server becomes available.
	InstallerStatusRunning = "RUNNING"

	// InstallerStatusFailed published each time installation failed.
	// Installation failed when:
	// - if installer defines server, but after period server is not available on port
	// - if installer does not define server and installation script exit code is different from 0.
	InstallerStatusFailed = "FAILED"
)

const (
	// StderrStream value of InstallerLogEvent.Stream if log line is from process STDERR.
	StderrStream = "STDERR"

	// StdoutStream value of InstallerLogEvent.Stream if log line is from process STDOUT.
	StdoutStream = "STDOUT"
)

// RuntimeID is an identifier of running workspace.
// Included to all the bootstrapper events.
type RuntimeID struct {
	// Workspace is an identifier of the workspace e.g. "workspace123456".
	Workspace string `json:"workspaceId"`

	// Environment is a name of environment e.g. "default".
	Environment string `json:"envName"`

	// OwnerId is an identifier of user who is runtime owner.
	OwnerId string `json:"ownerId"`
}

// MachineEvent is a base event for bootstrapper events.
type MachineEvent struct {

	// MachineName is a name of machine which this bootstrapper is deployed into.
	MachineName string `json:"machineName"`

	// RuntimeID indicated workspace runtime.
	RuntimeID RuntimeID `json:"runtimeId"`

	// Time when this event occurred.
	Time time.Time `json:"time"`
}

// StatusChangedEvent published each time bootstrapper status changed.
type StatusChangedEvent struct {
	MachineEvent

	// Status is current status of bootstrapper.
	Status string `json:"status"`

	// Error error message, can be present only if status is FAILED.
	Error string `json:"error,omitempty"`
}

// Type returns StatusChangedEventType.
func (e *StatusChangedEvent) Type() string { return StatusChangedEventType }

// InstallerStatusChangedEvent published for each installer when its status changed.
type InstallerStatusChangedEvent struct {
	MachineEvent

	// Status is current status of installer.
	Status string `json:"status"`

	// Installer is identifier of installer like "org.eclipse.che.ws-agent"
	Installer string `json:"installer"`

	// Error error message, can be present only if status is FAILED.
	Error string `json:"error,omitempty"`
}

// Type returns InstallerStatusChangedEventType.
func (e *InstallerStatusChangedEvent) Type() string { return InstallerStatusChangedEventType }

// InstallerLogEvent published each time installer writes to stderr/stdout.
type InstallerLogEvent struct {
	MachineEvent

	// Text is written by installation line of text.
	Text string `json:"text"`

	// Installer is identifier of installer like "org.eclipse.che.ws-agent"
	Installer string `json:"installer"`

	// Stream defines whether it is STDERR or STDOUT
	Stream string `json:"stream"`
}

// Type returns InstallerLogEventType.
func (e *InstallerLogEvent) Type() string { return InstallerLogEventType }
