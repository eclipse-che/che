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

import { DevfileChangeEventData } from '../devfile-change-event-data';

export interface IImportStackScopeBindings {
  onChange: IImportStackScopeOnChange;
}
export interface IImportStackScopeOnChange {
  (eventData: DevfileChangeEventData): void;
}

/**
 * Defines a directive for displaying import stack widget.
 *
 * @author Oleksii Orel
 */
export class ImportStack implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/import-custom-stack/import-custom-stack.html';
  controller: string = 'ImportStackController';
  controllerAs: string = 'importStackController';
  bindToController: boolean = true;

  transclude: boolean = true;

  scope: {
    onChange: string;
  };

  constructor() {
    this.scope = {
      onChange: '&'
    };
  }

}
