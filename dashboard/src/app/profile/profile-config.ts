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
import {ProfileController} from './profile.controller';

export class ProfileConfig {

  constructor(register: che.IRegisterService) {
    register.controller('ProfileController', ProfileController);

    let locationProvider = {
      title: 'Account',
      templateUrl: 'app/profile/profile.html',
      controller: 'ProfileController',
      controllerAs: 'profileController'
    };

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/account', locationProvider);
    }]);
  }
}
