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

package cfg

import (
	"encoding/json"
	"flag"
	"io/ioutil"
	"log"
	"os"
	"strconv"
	"strings"

	"github.com/eclipse/che/agents/go-agents/bootstrapper/booter"
)

var (
	// FilePath path to config file.
	FilePath string

	// PushStatusesEndpoint where to push statuses.
	PushStatusesEndpoint string

	// PushLogsEndpoint where to push logs.
	PushLogsEndpoint string

	// AuthEnabled whether authentication is needed
	AuthEnabled bool

	// Token to access wsmaster API
	Token string

	// RuntimeID the id of workspace runtime this machine belongs to.
	RuntimeID    booter.RuntimeID
	runtimeIDRaw string

	// MachineName is the name of this machine.
	MachineName string

	// InstallerTimeoutSec how much time(seconds) is given for one installation to complete.
	InstallerTimeoutSec int

	// CheckServersPeriodSec how much time(seconds) is between servers checks for one installer.
	CheckServersPeriodSec int

	// LogsEndpointReconnectPeriodSec how much time(seconds) is between logs endpoint reconnect attempts.
	LogsEndpointReconnectPeriodSec int

	// SelfSignedCertificateFilePath path to certificate file that should be used while connection establishing
	SelfSignedCertificateFilePath string
)

func init() {
	curDir, err := os.Getwd()
	if err != nil {
		log.Fatal(err)
	}
	flag.StringVar(
		&FilePath,
		"file",
		curDir+string(os.PathSeparator)+"config.json",
		"Path to configuration file on filesystem",
	)
	flag.StringVar(
		&PushStatusesEndpoint,
		"push-endpoint",
		"",
		"WebSocket endpoint where to push statuses",
	)
	flag.StringVar(
		&PushLogsEndpoint,
		"push-logs-endpoint",
		"",
		"WebSocket endpoint where to push logs",
	)
	// auth configuration
	defaultAuthEnabled := false
	authEnabledEnv := os.Getenv("CHE_AUTH_ENABLED")
	b, e := strconv.ParseBool(authEnabledEnv)
	if e == nil {
		defaultAuthEnabled = b
	}
	flag.BoolVar(
		&AuthEnabled,
		"enable-auth",
		defaultAuthEnabled,
		"whether authenticate requests on workspace master before allowing them to proceed."+
			"By default the value from 'CHE_AUTH_ENABLED' environment variable is used or `false` if it is missing",
	)
	flag.StringVar(
		&runtimeIDRaw,
		"runtime-id",
		"",
		"The identifier of the runtime in format 'workspace:environment:ownerId'",
	)
	flag.StringVar(
		&MachineName,
		"machine-name",
		"",
		"The name of the machine in which this bootstrapper is running",
	)
	flag.IntVar(
		&InstallerTimeoutSec,
		"installer-timeout",
		120, // 2m
		`Time(in seconds) given for one installer to complete its installation.
	If installation is not finished in time it will be interrupted`,
	)
	flag.IntVar(
		&CheckServersPeriodSec,
		"server-check-period",
		3,
		`Time(in seconds) between servers availability checks.
	Once servers for one installer available - checks stopped`,
	)
	flag.IntVar(
		&LogsEndpointReconnectPeriodSec,
		"logs-endpoint-reconnect-period",
		10,
		`Time(in seconds) between attempts to reconnect to push-logs-endpoint.
	Bootstrapper tries to reconnect to push-logs-endpoint when previously established connection lost`,
	)
	flag.StringVar(
		&SelfSignedCertificateFilePath,
		"cacert",
		"",
		"Path to Certificate that should be used while connection establishing",
	)
}

// Parse parses configuration.
func Parse() {
	flag.Parse()

	// push-endpoint
	if len(PushStatusesEndpoint) == 0 {
		log.Fatal("Push endpoint required(set it with -push-endpoint argument)")
	}
	if !strings.HasPrefix(PushStatusesEndpoint, "ws") {
		log.Fatal("Push endpoint protocol must be either ws or wss")
	}

	// push-logs-endpoint
	if len(PushLogsEndpoint) != 0 && !strings.HasPrefix(PushLogsEndpoint, "ws") {
		log.Fatal("Push logs endpoint protocol must be either ws or wss")
	}

	// auth-enabled - fetch CHE_MACHINE_TOKEN
	if AuthEnabled {
		Token = os.Getenv("CHE_MACHINE_TOKEN")
	}

	// runtime-id
	if len(runtimeIDRaw) == 0 {
		log.Fatal("Runtime ID required(set it with -runtime-id argument)")
	}
	parts := strings.Split(runtimeIDRaw, ":")
	if len(parts) != 3 {
		log.Fatalf("Expected runtime id to be in format 'workspace:env:ownerId'")
	}
	RuntimeID = booter.RuntimeID{Workspace: parts[0], Environment: parts[1], OwnerId: parts[2]}

	// machine-name
	if len(MachineName) == 0 {
		log.Fatal("Machine name required(set it with -machine-name argument)")
	}

	if InstallerTimeoutSec <= 0 {
		log.Fatal("Installer timeout must be > 0")
	}
	if CheckServersPeriodSec <= 0 {
		log.Fatal("Servers check period must be > 0")
	}
}

// Print prints configuration.
func Print() {
	log.Print("Bootstrapper configuration")
	log.Printf("  Push endpoint: %s", PushStatusesEndpoint)
	log.Printf("  Push logs endpoint: %s", PushLogsEndpoint)
	log.Printf("  Auth enabled: %t", AuthEnabled)
	if (SelfSignedCertificateFilePath != "") {
		log.Printf("  Self signed certificate %s", SelfSignedCertificateFilePath)
	}
	log.Print("  Runtime ID:")
	log.Printf("    Workspace: %s", RuntimeID.Workspace)
	log.Printf("    Environment: %s", RuntimeID.Environment)
	log.Printf("    OwnerId: %s", RuntimeID.OwnerId)
	log.Printf("  Machine name: %s", MachineName)
	log.Printf("  Installer timeout: %dseconds", InstallerTimeoutSec)
	log.Printf("  Check servers period: %dseconds", CheckServersPeriodSec)
	log.Printf("  Push logs endpoint reconnect period: %dseconds", LogsEndpointReconnectPeriodSec)
}

// ReadInstallersConfig reads content of file by path cfg.FilePath,
// parses its content as array of installers and returns it.
// If any error occurs during read, log.Fatal is called.
func ReadInstallersConfig() []booter.Installer {
	f, err := os.Open(FilePath)
	if err != nil {
		log.Fatal(err)
	}

	defer func() {
		if err := f.Close(); err != nil {
			log.Printf("Can't close installers config source, cause: %s", err)
		}
	}()

	raw, err := ioutil.ReadAll(f)
	if err != nil {
		log.Fatal(err)
	}

	installers := make([]booter.Installer, 0)
	if err := json.Unmarshal(raw, &installers); err != nil {
		log.Fatal(err)
	}
	return installers
}
