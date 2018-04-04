/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
