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

import {Register} from '../components/utils/register';
import {FactoryConfig} from './factories/factories-config';
import {ComponentsConfig} from '../components/components-config';
import {AdminsConfig} from './admin/admin-config';
import {AdministrationConfig} from './administration/administration-config';
import {DiagnosticsConfig} from './diagnostics/diagnostics-config';
import {CheColorsConfig} from './colors/che-color.constant';
import {CheOutputColorsConfig} from './colors/che-output-colors.constant';
import {CheCountriesConfig} from './constants/che-countries.constant';
import {CheJobsConfig} from './constants/che-jobs.constant';
import {DashboardConfig} from './dashboard/dashboard-config';
// switch to a config
import {IdeConfig} from './ide/ide-config';
import {NavbarConfig} from './navbar/navbar-config';
import {ProxySettingsConfig} from './proxy/proxy-settings.constant';
import {WorkspacesConfig} from './workspaces/workspaces-config';
import {StacksConfig} from './stacks/stacks-config';
import {DemoComponentsController} from './demo-components/demo-components.controller';
import {CheBranding} from '../components/branding/che-branding.factory';
import {ChePreferences} from '../components/api/che-preferences.factory';
import {RoutingRedirect} from '../components/routing/routing-redirect.factory';
import IdeIFrameSvc from './ide/ide-iframe/ide-iframe.service';
import {CheIdeFetcher} from '../components/ide-fetcher/che-ide-fetcher.service';
import {RouteHistory} from '../components/routing/route-history.service';
import {CheUIElementsInjectorService} from '../components/service/injector/che-ui-elements-injector.service';
import {OrganizationsConfig} from './organizations/organizations-config';
import {TeamsConfig} from './teams/teams-config';
import {ProfileConfig} from './profile/profile-config';

// init module
const initModule = angular.module('userDashboard', ['ngAnimate', 'ngCookies', 'ngTouch', 'ngSanitize', 'ngResource', 'ngRoute',
  'angular-websocket', 'ui.bootstrap', 'ngMaterial', 'ngMessages', 'angularMoment', 'angular.filter',
  'ngLodash', 'uuid4', 'angularFileUpload', 'ui.gravatar']);

window.name = 'NG_DEFER_BOOTSTRAP!';

declare const Keycloak: Function;
function buildKeycloakConfig(keycloakSettings: any): any {
  const theOidcProvider = keycloakSettings['che.keycloak.oidc_provider'];
  if (!theOidcProvider) {
    return {
      url: keycloakSettings['che.keycloak.auth_server_url'],
      realm: keycloakSettings['che.keycloak.realm'],
      clientId: keycloakSettings['che.keycloak.client_id']
    };
  } else {
    return {
      oidcProvider: theOidcProvider,
      clientId: keycloakSettings['che.keycloak.client_id']
    };
  }
}
interface IResolveFn<T> {
  (value?: T | PromiseLike<T>): void;
}
interface IRejectFn<T> {
  (reason?: any): void;
}
function keycloakLoad(keycloakSettings: any) {
  return new Promise((resolve: IResolveFn<any>, reject: IRejectFn<any>) => {
    const script = document.createElement('script');
    script.async = true;
    script.src = keycloakSettings['che.keycloak.js_adapter_url'];
    script.addEventListener('load', resolve);
    script.addEventListener('error', () => reject('Error loading script.'));
    script.addEventListener('abort', () => reject('Script loading aborted.'));
    document.head.appendChild(script);
  });
}

function keycloakInit(keycloakConfig: any, initOptions: any) {
  return new Promise((resolve: IResolveFn<any>, reject: IRejectFn<any>) => {
    const keycloak = Keycloak(keycloakConfig);
    window.sessionStorage.setItem('oidcDashboardRedirectUrl', location.href);
    keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false,
      useNonce: initOptions['useNonce'],
      scope: 'email profile',
      redirectUri: initOptions['redirectUrl']
    }).success(() => {
      resolve(keycloak);
    }).error((error: any) => {
      reject(error);
    });
  });
}
function setAuthorizationHeader(xhr: XMLHttpRequest, keycloak: any): Promise<any> {
  return new Promise((resolve: IResolveFn<any>, reject: IRejectFn<any>) => {
    if (keycloak && keycloak.token) {
      keycloak.updateToken(5).success(() => {
        xhr.setRequestHeader('Authorization', 'Bearer ' + keycloak.token);
        resolve(xhr);
      }).error(() => {
        console.log('Failed to refresh token');
        window.sessionStorage.setItem('oidcDashboardRedirectUrl', location.href);
        keycloak.login();
        reject('Authorization is needed.');
      });
      return;
    }

    resolve(xhr);
  });
}
function getApis(keycloak: any): Promise<void> {
  const request = new XMLHttpRequest();
  request.open('GET', '/api');
  return setAuthorizationHeader(request, keycloak).then((xhr: XMLHttpRequest) => {
    return new Promise<void>((resolve: IResolveFn<void>, reject: IRejectFn<void>) => {
      xhr.send();
      xhr.onreadystatechange = () => {
        if (xhr.readyState !== 4) { return; }
        if (xhr.status === 200) {
          resolve();
        } else {
          reject(xhr.responseText ? xhr.responseText : 'Unknown error');
        }
      };
    });
  });
}
function showErrorMessage(errorMessage: string) {
  const div = document.createElement('p');
  div.className = 'authorization-error';
  div.innerHTML = errorMessage + '<br/>Click <a href="/">here</a> to reload page.';
  document.querySelector('.main-page-loader').appendChild(div);
}

const keycloakAuth = {
  isPresent: false,
  keycloak: null,
  config: null
};
initModule.constant('keycloakAuth', keycloakAuth);

angular.element(document).ready(() => {
  const promise = new Promise((resolve: IResolveFn<any>, reject: IRejectFn<any>) => {
    angular.element.get('/api/keycloak/settings').then(resolve, reject);
  });
  promise.then((keycloakSettings: any) => {
    keycloakAuth.config = buildKeycloakConfig(keycloakSettings);

    // load Keycloak
    return keycloakLoad(keycloakSettings).then(() => {
      // init Keycloak
      var theUseNonce: boolean;
      if (typeof keycloakSettings['che.keycloak.use_nonce'] === 'string') {
        theUseNonce = keycloakSettings['che.keycloak.use_nonce'].toLowerCase() === 'true';
      }
      var initOptions = {
        useNonce: theUseNonce,
        redirectUrl: keycloakSettings['che.keycloak.redirect_url.dashboard']
      }
      return keycloakInit(keycloakAuth.config, initOptions);
    }).then((keycloak: any) => {
      keycloakAuth.isPresent = true;
      keycloakAuth.keycloak = keycloak;
      /* tslint:disable */
      window['_keycloak'] = keycloak;
      /* tslint:enable */
    });
  }).catch((error: any) => {
    console.error('Keycloak initialization failed with error: ', error);
  }).then(() => {
    const keycloak = (window as any)._keycloak;
    // try to reach the API
    // to check if user is authorized to do that
    return getApis(keycloak);
  }).then(() => {
    (angular as any).resumeBootstrap();
  }).catch((error: string) => {
    console.error(`Can't GET "/api". ${error ? 'Error: ' : ''}`, error);
    showErrorMessage(error);
  });
});

// add a global resolve flag on all routes (user needs to be resolved first)
initModule.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
  $routeProvider.accessWhen = (path: string, route: che.route.IRoute) => {
    if (angular.isUndefined(route.resolve)) {
      route.resolve = {};
    }
    (route.resolve as any).app = ['cheBranding', '$q', 'chePreferences', (cheBranding: CheBranding, $q: ng.IQService, chePreferences: ChePreferences) => {
      const deferred = $q.defer();
      if (chePreferences.getPreferences()) {
        deferred.resolve();
      } else {
        chePreferences.fetchPreferences().then(() => {
          deferred.resolve();
        }, (error: any) => {
          deferred.reject(error);
        });
      }
      return deferred.promise;
    }];

    return $routeProvider.when(path, route);
  };

  $routeProvider.accessOtherWise = (route: che.route.IRoute) => {
    if (angular.isUndefined(route.resolve)) {
      route.resolve = {};
    }
    (route.resolve as any).app = ['$q', 'chePreferences', ($q: ng.IQService, chePreferences: ChePreferences) => {
      const deferred = $q.defer();
      if (chePreferences.getPreferences()) {
        deferred.resolve();
      } else {
        chePreferences.fetchPreferences().then(() => {
          deferred.resolve();
        }, (error: any) => {
          deferred.reject(error);
        });
      }
      return deferred.promise;
    }];
    return $routeProvider.otherwise(route);
  };


}]);

const DEV = false;

// configs
initModule.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
  // config routes (add demo page)
  if (DEV) {
    $routeProvider.accessWhen('/demo-components', {
      title: 'Demo Components',
      templateUrl: 'app/demo-components/demo-components.html',
      controller: 'DemoComponentsController',
      controllerAs: 'demoComponentsController',
      reloadOnSearch: false
    });
  }

  $routeProvider.accessOtherWise({
    redirectTo: '/workspaces'
  });
}]);

/**
 * Setup route redirect module
 */
initModule.run(['$rootScope', '$location', '$routeParams', 'routingRedirect', '$timeout', 'ideIFrameSvc', 'cheIdeFetcher', 'routeHistory', 'cheUIElementsInjectorService', 'workspaceDetailsService',
  ($rootScope: che.IRootScopeService, $location: ng.ILocationService, $routeParams: ng.route.IRouteParamsService, routingRedirect: RoutingRedirect, $timeout: ng.ITimeoutService, ideIFrameSvc: IdeIFrameSvc, cheIdeFetcher: CheIdeFetcher, routeHistory: RouteHistory, cheUIElementsInjectorService: CheUIElementsInjectorService) => {
    $rootScope.hideLoader = false;
    $rootScope.waitingLoaded = false;
    $rootScope.showIDE = false;

    // here only to create instances of these components
    /* tslint:disable */
    cheIdeFetcher;
    routeHistory;
    /* tslint:enable */

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

    $rootScope.$on('$routeChangeStart', (event: any, next: any) => {
      if (DEV) {
        console.log('$routeChangeStart event with route', next);
      }
    });

    $rootScope.$on('$routeChangeSuccess', (event: ng.IAngularEvent, next: ng.route.IRoute) => {
      const route = (<any>next).$$route;
      if (angular.isFunction(route.title)) {
        $rootScope.currentPage = route.title($routeParams);
      } else {
        $rootScope.currentPage = route.title || 'Dashboard';
      }
      const originalPath: string = route.originalPath;
      if (originalPath && originalPath.indexOf('/ide/') === -1) {
        $rootScope.showIDE = false;
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
  }
]);

initModule.config(['$mdThemingProvider', 'jsonColors', ($mdThemingProvider: ng.material.IThemingProvider, jsonColors: any) => {

  const cheColors = angular.fromJson(jsonColors);
  const getColor = (key: string) => {
    let color = cheColors[key];
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

  const cheMap = $mdThemingProvider.extendPalette('indigo', {
    '500': getColor('$dark-menu-color'),
    '300': 'D0D0D0'
  });
  $mdThemingProvider.definePalette('che', cheMap);

  const cheDangerMap = $mdThemingProvider.extendPalette('red', {});
  $mdThemingProvider.definePalette('cheDanger', cheDangerMap);

  const cheWarningMap = $mdThemingProvider.extendPalette('orange', {
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheWarning', cheWarningMap);

  const cheGreenMap = $mdThemingProvider.extendPalette('green', {
    'A100': '#46AF00',
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheGreen', cheGreenMap);

  const cheDefaultMap = $mdThemingProvider.extendPalette('blue', {
    'A400': getColor('$che-medium-blue-color')
  });
  $mdThemingProvider.definePalette('cheDefault', cheDefaultMap);

  const cheNoticeMap = $mdThemingProvider.extendPalette('blue', {
    'A400': getColor('$mouse-gray-color')
  });
  $mdThemingProvider.definePalette('cheNotice', cheNoticeMap);

  const cheAccentMap = $mdThemingProvider.extendPalette('blue', {
    '700': getColor('$che-medium-blue-color'),
    'A400': getColor('$che-medium-blue-color'),
    'A200': getColor('$che-medium-blue-color'),
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheAccent', cheAccentMap);

  const cheNavyPalette = $mdThemingProvider.extendPalette('purple', {
    '500': getColor('$che-navy-color'),
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('cheNavyPalette', cheNavyPalette);

  const toolbarPrimaryPalette = $mdThemingProvider.extendPalette('purple', {
    '500': getColor('$che-white-color'),
    'contrastDefaultColor': 'dark'
  });
  $mdThemingProvider.definePalette('toolbarPrimaryPalette', toolbarPrimaryPalette);

  const toolbarAccentPalette = $mdThemingProvider.extendPalette('purple', {
    'A200': 'EF6C00',
    '700': 'E65100',
    'contrastDefaultColor': 'light'
  });
  $mdThemingProvider.definePalette('toolbarAccentPalette', toolbarAccentPalette);

  const cheGreyPalette = $mdThemingProvider.extendPalette('grey', {
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
}]);

initModule.constant('userDashboardConfig', {
  developmentMode: DEV
});

const instanceRegister = new Register(initModule);

if (DEV) {
  instanceRegister.controller('DemoComponentsController', DemoComponentsController);
}

/* tslint:disable */
new ProxySettingsConfig(instanceRegister);
new CheColorsConfig(instanceRegister);
new CheOutputColorsConfig(instanceRegister);
new CheCountriesConfig(instanceRegister);
new CheJobsConfig(instanceRegister);
new ComponentsConfig(instanceRegister);
new AdminsConfig(instanceRegister);
new AdministrationConfig(instanceRegister);
new IdeConfig(instanceRegister);
new DiagnosticsConfig(instanceRegister);

new NavbarConfig(instanceRegister);
new WorkspacesConfig(instanceRegister);
new DashboardConfig(instanceRegister);
new StacksConfig(instanceRegister);
new FactoryConfig(instanceRegister);
new OrganizationsConfig(instanceRegister);
new TeamsConfig(instanceRegister);
new ProfileConfig(instanceRegister);
/* tslint:enable */
