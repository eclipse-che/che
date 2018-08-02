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

package common

import "log"

// LogError logs error if it is not nil.
// Useful in situations when error is not processed in any case but its logging is needed.
func LogError(err error) {
	if err != nil {
		log.Println(err)
	}
}
