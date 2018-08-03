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
 * Defines a directive for displaying machines RAM settings.
 *
 * @author Oleksii Kurinnyi
 */
export class RamSettings implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/ram-settings/ram-settings.html';
  replace: boolean = false;

  controller: string = 'RamSettingsController';
  controllerAs: string = 'ramSettingsController';

  bindToController: boolean = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      machines: '=',
      environmentManager: '=',
      onRamChange: '&'
    };
  }
}
