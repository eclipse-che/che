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

package main

import (
	"fmt"
)

const COLOR_RED = "\x1b[31;1m "
const COLOR_GREEN = "\x1b[32;1m "
const COLOR_YELLOW = "\x1b[33;1m "
const COLOR_BLACK = "\x1b[34;1m "

func Print(color string, s string) {
	fmt.Printf("%s %s\n", color, s)
}
