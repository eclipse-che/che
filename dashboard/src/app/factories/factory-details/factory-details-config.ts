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

import {FactoryDetailsController} from '../factory-details/factory-details.controller';
import {InformationTabConfig} from './information-tab/information-tab-config';


export class FactoryDetailsConfig {

  constructor(register: che.IRegisterService) {
    register.controller('FactoryDetailsController', FactoryDetailsController);

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      let locationProvider = {
        title: 'Factory',
        templateUrl: 'app/factories/factory-details/factory-details.html',
        controller: 'FactoryDetailsController',
        controllerAs: 'factoryDetailsController'
      };

      $routeProvider.accessWhen('/factory/:id', locationProvider)
        .accessWhen('/factory/:id/:tabName', locationProvider);

    }]);

    // config files
    /* tslint:disable */
    new InformationTabConfig(register);
    /* tslint:enable */
  }
}
