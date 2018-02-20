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
