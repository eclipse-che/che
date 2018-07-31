//
// Copyright (c) 2012-2018 Red Hat, Inc.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
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
