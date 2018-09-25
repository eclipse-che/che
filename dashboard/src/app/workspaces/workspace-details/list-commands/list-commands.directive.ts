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
 * @ngdoc directive
 * @name workspaces.details.directive:listCommands
 * @restrict E
 * @element
 *
 * @description
 * `<list-commands commands="ctrl.commands"></list-commands>` for displaying list of commands
 *
 * @usage
 *   <list-commands commands="ctrl.commands"></list-commands>
 *
 * @author Oleksii Orel
 */
export class ListCommands {
  bindToController: boolean;
  restrict: string;
  templateUrl: string;
  controller: string;
  controllerAs: string;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor () {
    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/workspace-details/list-commands/list-commands.html';

    this.controller = 'ListCommandsController';
    this.controllerAs = 'listCommandsController';
    this.bindToController = true;

    // scope values
    this.scope = {
      commands: '=',
      commandsOnChange: '&'
    };
  }
}
