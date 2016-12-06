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

import {Register} from '../components/utils/register';

import {ComponentsConfig} from '../components/components-config';

import {AdminsConfig} from './admin/admin-config';
import {AdministrationConfig} from './administration/administration-config';
import {CheColorsConfig} from './colors/che-color.constant';
import {CheOutputColorsConfig} from './colors/che-output-colors.constant';
import {CheCountriesConfig} from './constants/che-countries.constant';
import {CheJobsConfig} from './constants/che-jobs.constant';
import {DashboardConfig} from './dashboard/dashboard-config';
// switch to a config
import {IdeConfig} from './ide/ide-config';
import {NavbarConfig} from './navbar/navbar-config';
import {ProjectsConfig} from './projects/projects-config';
import {ProxySettingsConfig} from './proxy/proxy-settings.constant';
import {WorkspacesConfig} from './workspaces/workspaces-config';
import {StacksConfig} from './stacks/stacks-config';


// init module
let initModule = angular.module('userDashboard', ['ngAnimate', 'ngCookies', 'ngTouch', 'ngSanitize', 'ngResource', 'ngRoute',
  'angular-websocket', 'ui.bootstrap', 'ui.codemirror', 'ngMaterial', 'ngMessages', 'angularMoment', 'angular.filter',
  'ngDropdowns', 'ngLodash', 'angularCharts', 'ngClipboard', 'uuid4', 'angularFileUpload']);


// add a global resolve flag on all routes (user needs to be resolved first)
initModule.config(['$routeProvider', ($routeProvider) => {
  $routeProvider.accessWhen = (path, route) => {
    route.resolve || (route.resolve = {});
    route.resolve.app = ['cheBranding', '$q', 'chePreferences', (cheBranding, $q, chePreferences) => {
      let deferred = $q.defer();
      if (chePreferences.getPreferences()) {
        deferred.resolve();
      } else {
        chePreferences.fetchPreferences().then(() => {
          deferred.resolve();
        }, (error) => {
          deferred.reject(error);
        });
      }

      return deferred.promise;
    }];

    return $routeProvider.when(path, route);
  };

  $routeProvider.accessOtherWise = (route) => {
    route.resolve || (route.resolve = {});
    route.resolve.app = ['$q', 'chePreferences', ($q, chePreferences) => {
      let deferred = $q.defer();
      if (chePreferences.getPreferences()) {
        deferred.resolve();
      } else {
        chePreferences.fetchPreferences().then(() => {
          deferred.resolve();
        }, (error) => {
          deferred.reject(error);
        });
      }

      return deferred.promise;
    }];
    return $routeProvider.otherwise(route);
  };


}]);

var DEV = false;


// configs
initModule.config(['$routeProvider', 'ngClipProvider', ($routeProvider, ngClipProvider) => {
  // config routes (add demo page)
  if (DEV) {
    $routeProvider.accessWhen('/demo-components', {
      title: 'Demo Components',
      templateUrl: 'app/demo-components/demo-components.html',
      controller: 'DemoComponentsCtrl',
      controllerAs: 'demoComponentsCtrl'
    });
  }

  $routeProvider.accessOtherWise({
    redirectTo: '/workspaces'
  });
  // add .swf path location using ngClipProvider
  let ngClipProviderPath = DEV ? 'bower_components/zeroclipboard/dist/ZeroClipboard.swf' : 'assets/zeroclipboard/ZeroClipboard.swf';
  ngClipProvider.setPath(ngClipProviderPath);
}]);


/**
 * Setup route redirect module
 */
initModule.run(['$rootScope', '$location', '$routeParams', 'routingRedirect', '$timeout', 'ideIFrameSvc', 'cheIdeFetcher', 'routeHistory', 'cheUIElementsInjectorService', 'workspaceDetailsService',
  ($rootScope, $location, $routeParams, routingRedirect, $timeout, ideIFrameSvc, cheIdeFetcher, routeHistory, cheUIElementsInjectorService, workspaceDetailsService) => {
    $rootScope.hideLoader = false;
    $rootScope.waitingLoaded = false;
    $rootScope.showIDE = false;

    workspaceDetailsService.addSection('Projects', '<workspace-details-projects></workspace-details-projects>', 'icon-ic_inbox_24px');
    workspaceDetailsService.addSection('SSH', '<workspace-details-ssh></workspace-details-ssh>', 'icon-ic_vpn_key_24px');

    // here only to create instances of these components
    cheIdeFetcher;
    routeHistory;

    $rootScope.$on('$viewContentLoaded', () => {
      cheUIElementsInjectorService.injectAll();
      $timeout(() => {
        if (!$rootScope.hideLoader) {
          if (!$rootScope.wantTokeepLoader) {
            $rootScope.hideLoader = true;
          } else {
            $rootScope.hideLoader = false;
          }
        }
        $rootScope.waitingLoaded = true;
      }, 1000);
    });

    $rootScope.$on('$routeChangeStart', (event, next)=> {
      if (DEV) {
        console.log('$routeChangeStart event with route', next);
      }
    });


    $rootScope.$on('$routeChangeSuccess', (event, next) => {
      if (next.$$route.title && angular.isFunction(next.$$route.title)) {
        $rootScope.currentPage = next.$$route.title($routeParams);
      } else {
        $rootScope.currentPage = next.$$route.title || 'Dashboard';
      }

      // when a route is about to change, notify the routing redirect node
      if (next.resolve) {
        if (DEV) {
          console.log('$routeChangeSuccess event with route', next);
        }// check routes
        routingRedirect.check(event, next);
      }
    });

    $rootScope.$on('$routeChangeError', () => {
      $location.path('/');
    });
  }]);


// add interceptors
initModule.factory('ETagInterceptor', ($window, $cookies, $q) => {

  var etagMap = {};

  return {
    request: (config) => {
      // add IfNoneMatch request on the che api if there is an existing eTag
      if ('GET' === config.method) {
        if (config.url.indexOf('/api') === 0) {
          let eTagURI = etagMap[config.url];
          if (eTagURI) {
            config.headers = config.headers || {};
            angular.extend(config.headers, {'If-None-Match': eTagURI});
          }
        }
      }
      return config || $q.when(config);
    },
    response: (response) => {

      // if response is ok, keep ETag
      if ('GET' === response.config.method) {
        if (response.status === 200) {
          var responseEtag = response.headers().etag;
          if (responseEtag) {
            if (response.config.url.indexOf('/api') === 0) {

              etagMap[response.config.url] = responseEtag;
            }
          }
        }

      }
      return response || $q.when(response);
    }
  };
});

initModule.config(($mdThemingProvider, jsonColors) => {

  var cheColors = angular.fromJson(jsonColors);
  var getColor = (key) => {
    var color = cheColors[key];
    if (!color) {
      // return a flashy red color if color is undefined
      console.log('error, the color' + key + 'is undefined');
      return '#ff0000';
    }
    if (color.indexOf('$') === 0) {
      color = getColor(color);
    }
    return color;

  };


  var cheMap = $mdThemingProvider.extendPalette('indigo', {
    '500': getColor('$dark-menu-color'),
    '300': 'D0D0D0'
  });
  $mdThemingProvider.definePalette('che', cheMap);

  var cheDangerMap = $mdThemingProvider.extendPalette('red', {});
  $mdThemingProvider.definePalette('cheDanger', cheDangerMap);

  var cheWarningMap = $mdThemingProvider.extendPalette('orange', {
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheWarning', cheWarningMap);

  var cheGreenMap = $mdThemingProvider.extendPalette('green', {
    'A100': '#46AF00',
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheGreen', cheGreenMap);

  var cheDefaultMap = $mdThemingProvider.extendPalette('blue', {
    'A400': getColor('$che-medium-blue-color')
  });
  $mdThemingProvider.definePalette('cheDefault', cheDefaultMap);

  var cheNoticeMap = $mdThemingProvider.extendPalette('blue', {
    'A400': getColor('$mouse-gray-color')
  });
  $mdThemingProvider.definePalette('cheNotice', cheNoticeMap);

  var cheAccentMap = $mdThemingProvider.extendPalette('blue', {
    '700': getColor('$che-medium-blue-color'),
    'A400': getColor('$che-medium-blue-color'),
    'A200': getColor('$che-medium-blue-color'),
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheAccent', cheAccentMap);


  var cheNavyPalette = $mdThemingProvider.extendPalette('purple', {
    '500': getColor('$che-navy-color'),
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheNavyPalette', cheNavyPalette);


  var toolbarPrimaryPalette = $mdThemingProvider.extendPalette('purple', {
    '500': getColor('$che-white-color'),
    'contrastDefaultColor': 'dark'
  });
  $mdThemingProvider.definePalette('toolbarPrimaryPalette', toolbarPrimaryPalette);

  var toolbarAccentPalette = $mdThemingProvider.extendPalette('purple', {
    'A200': 'EF6C00',
    '700': 'E65100',
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('toolbarAccentPalette', toolbarAccentPalette);

  var cheGreyPalette = $mdThemingProvider.extendPalette('grey', {
    'A100': 'efefef',
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheGrey', cheGreyPalette);

  $mdThemingProvider.theme('danger')
    .primaryPalette('che')
    .accentPalette('cheDanger')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('warning')
    .primaryPalette('che')
    .accentPalette('cheWarning')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('chesave')
    .primaryPalette('green')
    .accentPalette('cheGreen')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('checancel')
    .primaryPalette('che')
    .accentPalette('cheGrey')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('chedefault')
    .primaryPalette('che')
    .accentPalette('cheDefault')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('chenotice')
    .primaryPalette('che')
    .accentPalette('cheNotice')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('default')
    .primaryPalette('che')
    .accentPalette('cheAccent')
    .backgroundPalette('grey');

  $mdThemingProvider.theme('toolbar-theme')
    .primaryPalette('toolbarPrimaryPalette')
    .accentPalette('toolbarAccentPalette');

  $mdThemingProvider.theme('factory-theme')
    .primaryPalette('light-blue')
    .accentPalette('pink')
    .warnPalette('red')
    .backgroundPalette('purple');

  $mdThemingProvider.theme('onboarding-theme')
    .primaryPalette('cheNavyPalette')
    .accentPalette('pink')
    .warnPalette('red')
    .backgroundPalette('purple');

  $mdThemingProvider.theme('dashboard-theme')
    .primaryPalette('cheNavyPalette')
    .accentPalette('pink')
    .warnPalette('red')
    .backgroundPalette('purple');

  $mdThemingProvider.theme('maincontent-theme')
    .primaryPalette('che')
    .accentPalette('cheAccent');
});

initModule.constant('userDashboardConfig', {
  developmentMode: DEV
});

initModule.config(['$routeProvider', '$locationProvider', '$httpProvider', ($routeProvider, $locationProvider, $httpProvider) => {
  // add the ETag interceptor for Che API
  $httpProvider.interceptors.push('ETagInterceptor');
}]);


var instanceRegister = new Register(initModule);

new ProxySettingsConfig(instanceRegister);
new CheColorsConfig(instanceRegister);
new CheOutputColorsConfig(instanceRegister);
new CheCountriesConfig(instanceRegister);
new CheJobsConfig(instanceRegister);
new ComponentsConfig(instanceRegister);
new AdminsConfig(instanceRegister);
new AdministrationConfig(instanceRegister);
new IdeConfig(instanceRegister);

new NavbarConfig(instanceRegister);
new ProjectsConfig(instanceRegister);
new WorkspacesConfig(instanceRegister);
new DashboardConfig(instanceRegister);
new StacksConfig(instanceRegister);
