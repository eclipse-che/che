/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
   * @ngInject for Dependency injection
   */
  constructor () {
    // scope values
    this.scope = {
      servers: '=',
      serversOnChange: '&'
    };
  }
}
