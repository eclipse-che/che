/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
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
  scope: Object;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
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
