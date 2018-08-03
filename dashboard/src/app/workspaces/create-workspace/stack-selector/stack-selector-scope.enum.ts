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
 * Describes scopes of stacks
 *
 * @author Oleksii Kurinnyi
 */
export enum StackSelectorScope {
  ALL = 1,
  QUICK_START,
  SINGLE_MACHINE,
  MULTI_MACHINE
}
export namespace StackSelectorScope {
  export function keys(): string[] {
    return [
      StackSelectorScope[StackSelectorScope.ALL].toString(),
      StackSelectorScope[StackSelectorScope.QUICK_START].toString(),
      StackSelectorScope[StackSelectorScope.SINGLE_MACHINE].toString(),
      StackSelectorScope[StackSelectorScope.MULTI_MACHINE].toString()
    ];
  }
  export function values(): StackSelectorScope[] {
    return [
      StackSelectorScope.ALL,
      StackSelectorScope.QUICK_START,
      StackSelectorScope.SINGLE_MACHINE,
      StackSelectorScope.MULTI_MACHINE
    ];
  }
}
