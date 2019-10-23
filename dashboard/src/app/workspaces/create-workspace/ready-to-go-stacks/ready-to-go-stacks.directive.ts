/*
 * Copyright (c) 2015-2019 Red Hat, Inc.
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

export interface IReadyToGoStacksScopeBindings {
  onChange: Function;
}

export class ReadyStacksDirective implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/ready-to-go-stacks/ready-to-go-stacks.html';
  controller: string = 'ReadyToGoStacksController';
  controllerAs: string = 'readyToGoStacksController';
  bindToController: boolean = true;

  // TODO specify scope properties
  scope: {
    onChange: string;
  };

  constructor() {
    this.scope = {
      // TODO
      onChange: '&onChange'
    };
  }

}
