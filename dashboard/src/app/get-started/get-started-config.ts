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

import { GetStartedController } from './get-started.controller';
import { GetStartedConfigService } from './get-started-config.service';
import { GetStartedToolbarComponent } from './toolbar/get-started-toolbar.component';
import { GetStartedToolbarController } from './toolbar/get-started-toolbar.controller';
import { SampleCardDirective } from './sample-card/sample-card.directive';

/**
 * @name getStarted:GetStartedConfig
 * @description This class is used for configuring all get started devfiles.
 * @author Oleksii Orel
 */
export class GetStartedConfig {

  constructor(register: che.IRegisterService) {
    register.directive('sampleCard', SampleCardDirective);
    register.controller('GetStartedController', GetStartedController);
    register.controller('GetStartedToolbarController', GetStartedToolbarController);
    register.component('getStartedToolbar', GetStartedToolbarComponent);

    register.service('getStartedConfigService', GetStartedConfigService);

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: any) => {
      $routeProvider.accessWhen('/getstarted', {
        title: 'Get Started',
        templateUrl: 'app/get-started/get-started.html',
        controller: 'GetStartedController',
        controllerAs: 'getStartedController',
        resolve: {
          initData: ['getStartedConfigService', (svc: GetStartedConfigService) => {
            return svc.allowGetStartedRoutes();
          }]
        }
      });
    }]);
  }
}
