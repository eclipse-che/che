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

/**
 * This is class of remote page labels.
 *
 * @author Oleksii Orel
 */
export class RemotePageLabels {

  static get FIRST(): string {
    return 'first';
  }

  static get NEXT(): string {
    return 'next';
  }

  static get LAST(): string {
    return 'last';
  }

  static get PREVIOUS(): string {
    return 'prev';
  }

  static getValues(): Array<string> {
    return [
      RemotePageLabels.FIRST,
      RemotePageLabels.NEXT,
      RemotePageLabels.LAST,
      RemotePageLabels.PREVIOUS
    ];
  }

}
