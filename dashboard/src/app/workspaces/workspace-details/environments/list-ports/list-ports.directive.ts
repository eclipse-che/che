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
 * @name workspaces.details.directive:listPorts
 * @restrict E
 * @element
 *
 * @description
 * `<list-ports ports="ctrl.ports" ports-on-change="ctrl.onChangeCallback()"></list-ports>` for displaying list of ports
 *
 * @usage
 *   <list-ports ports="ctrl.ports" ports-on-change="ctrl.onChangeCallback()"></list-ports>
 *
 * @author Oleksii Kurinnyi
 */
export class ListPorts {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';
    this.templateUrl = 'app/workspaces/workspace-details/environments/list-ports/list-ports.html';

    this.controller = 'ListPortsController';
    this.controllerAs = 'listPortsController';
    this.bindToController = true;

    // scope values
    this.scope = {
      servers: '=',
      serversOnChange: '&'
    };
  }
}
