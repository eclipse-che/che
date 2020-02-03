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

import {TemplateListController} from './template-list/template-list.controller';
import {Template} from './template/template.directive';
import { CheDashboardConfigurationService } from '../../components/branding/che-dashboard-configuration.service';
import { GetStartedConfigService } from './get-started-config.service';

/**
 * @name getStarted:GetStartedConfig
 * @description This class is used for configuring all get started devfiles.
 * @author Oleksii Orel
 */
export class GetStartedConfig {

  constructor(register: che.IRegisterService) {
    register.directive('cheTemplate', Template);

    register.controller('TemplateListController', TemplateListController);

    register.service('getStartedConfigService', GetStartedConfigService);

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: any) => {
      $routeProvider.accessWhen('/getstarted', {
        title: 'Get Started',
        templateUrl: 'app/get-started/template-list/template-list.html',
        controller: 'TemplateListController',
        controllerAs: 'templateListController',
        resolve: {
          initData: ['getStartedConfigService', (svc: GetStartedConfigService) => {
            return svc.allowGetStartedRoutes();
          }]
        }
      });
    }]);
  }
}
