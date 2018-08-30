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


import {DockerRegistryList} from './docker-registry/docker-registry-list/docker-registry-list.directive';
import {DockerRegistryListController} from './docker-registry/docker-registry-list/docker-registry-list.controller';
import {EditRegistryController} from './docker-registry/docker-registry-list/edit-registry/edit-registry.controller';


export class AdministrationConfig {

  constructor(register: che.IRegisterService) {
    register.directive('dockerRegistryList', DockerRegistryList);
    register.controller('DockerRegistryListController', DockerRegistryListController);

    register.controller('EditRegistryController', EditRegistryController);

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/administration', {
        title: 'Administration',
        templateUrl: 'app/administration/administration.html'
      });
    }]);

  }
}
