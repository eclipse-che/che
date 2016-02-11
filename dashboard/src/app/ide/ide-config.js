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

/*exported IdeCtrl, IdeSvc */

import IdeCtrl from './ide.controller';
import IdeSvc from './ide.service';

import IdeLoaderCtrl from './ide-loader/ide-loader.controller';
import IdeLoader from './ide-loader/ide-loader.directive';
import IdeLoaderSvc from './ide-loader/ide-loader.service';

import IdeIFrameCtrl from './ide-iframe/ide-iframe.controller';
import IdeIFrame from './ide-iframe/ide-iframe.directive';
import IdeIFrameSvc from './ide-iframe/ide-iframe.service';

import IdeListItemNavbarCtrl from './ide-list-item-navbar/ide-list-item-navbar.controller';
import IdeListItemNavbar from './ide-list-item-navbar/ide-list-item-navbar.directive';

import IdeIFrameButtonLinkCtrl from './ide-iframe-button-link/ide-iframe-button-link.controller';
import IdeIFrameButtonLink from './ide-iframe-button-link/ide-iframe-button-link.directive';

export class IdeConfig {

  constructor(register) {
    register.service('ideSvc', IdeSvc);
    register.controller('IdeCtrl', IdeCtrl);


    register.service('ideLoaderSvc', IdeLoaderSvc);
    register.controller('IdeLoaderCtrl', IdeLoaderCtrl);
    register.directive('ideLoader', IdeLoader);

    register.service('ideIFrameSvc', IdeIFrameSvc);
    register.controller('IdeIFrameCtrl', IdeIFrameCtrl);
    register.directive('ideIframe', IdeIFrame);

    register.controller('IdeListItemNavbarCtrl', IdeListItemNavbarCtrl);
    register.directive('ideListItemNavbar', IdeListItemNavbar);


    register.controller('IdeIFrameButtonLinkCtrl', IdeIFrameButtonLinkCtrl);
    register.directive('ideIframeButtonLink', IdeIFrameButtonLink);

    let ideProvider = {
      templateUrl: 'app/ide/ide.html',
      controller: 'IdeCtrl',
      controllerAs: 'ideCtrl'
    };

    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/ide', ideProvider)
        .accessWhen('/ide/:workspaceName', ideProvider);

    });
  }
}

