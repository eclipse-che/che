/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

export enum ProjectSource { SAMPLES = 1, BLANK, GIT, GITHUB, ZIP }

export namespace ProjectSource {
  export function keys(): string[] {
    return [
      ProjectSource[ProjectSource.SAMPLES].toString(),
      ProjectSource[ProjectSource.BLANK].toString(),
      ProjectSource[ProjectSource.GIT].toString(),
      ProjectSource[ProjectSource.GITHUB].toString(),
      ProjectSource[ProjectSource.ZIP].toString()
    ];
  }
  export function values(): ProjectSource[] {
    return [
      ProjectSource.SAMPLES,
      ProjectSource.BLANK,
      ProjectSource.GIT,
      ProjectSource.GITHUB,
      ProjectSource.ZIP
    ];
  }
}
