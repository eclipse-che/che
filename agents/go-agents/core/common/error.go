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

package common

import "log"

// LogError logs error if it is not nil.
// Useful in situations when error is not processed in any case but its logging is needed.
func LogError(err error) {
	if err != nil {
		log.Println(err)
	}
}
