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
 * @name workspaces.details.directive:listServers
 * @restrict E
 * @element
 *
 * @description
 * `<list-servers ports="ctrl.ports" ports-on-change="ctrl.onChangeCallback()"></list-servers>` for displaying list of ports
 *
 * @usage
 *   <list-servers ports="ctrl.ports" ports-on-change="ctrl.onChangeCallback()"></list-servers>
 *
 * @author Oleksii Kurinnyi
 */
export class ListServers {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/environments/list-servers/list-servers.html';

  controller: string = 'ListServersController';
  controllerAs: string = 'listServersController';
  bindToController: boolean = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor () {
    // scope values
    this.scope = {
      servers: '=',
      serversOnChange: '&'
    };
  }
}
