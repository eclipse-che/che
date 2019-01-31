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
 * This is constants of machine source types.
 *
 *  @author Ann Shumilova
 */
class CheMachineSourceTypesStatic {

  static get TOOL(): string {
    return 'tool';
  }

  static get RECIPE(): string {
    return 'recipe';
  }

  static getValues(): Array<string> {
    return [
      CheMachineSourceTypesStatic.TOOL,
      CheMachineSourceTypesStatic.RECIPE
    ];
  }

}

export const CheMachineSourceTypes: che.resource.ICheMachineSourceTypes = CheMachineSourceTypesStatic;
