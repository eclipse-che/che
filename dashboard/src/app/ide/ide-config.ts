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

/*exported IdeCtrl, IdeSvc */

import IdeCtrl from './ide.controller';
import IdeSvc from './ide.service';
import IdeIFrame from './ide-iframe/ide-iframe.directive';
import IdeIFrameSvc from './ide-iframe/ide-iframe.service';

export class IdeConfig {

  constructor(register: che.IRegisterService) {
    register.service('ideSvc', IdeSvc);
    register.controller('IdeCtrl', IdeCtrl);

    register.service('ideIFrameSvc', IdeIFrameSvc);
    register.directive('ideIframe', IdeIFrame);

    const ideProvider = {
      title: (params: any) => {
        return params.workspaceName;
      },
      templateUrl: 'app/ide/ide.html',
      controller: 'IdeCtrl',
      controllerAs: 'ideCtrl'
    };

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/ide', ideProvider)
        .accessWhen('/ide/:namespace*/:workspaceName', ideProvider);
    }]);
  }
}

