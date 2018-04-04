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

import {KeycloakTokenInterceptor} from './keycloak-token-interceptor';
import {ETagInterceptor} from './e-tag-interceptor';

export class InterceptorConfig {

  constructor(register: che.IRegisterService) {
    register.factory('eTagInterceptor', ETagInterceptor);
    register.factory('keycloakTokenInterceptor', KeycloakTokenInterceptor);

    register.app.config(['$routeProvider', '$httpProvider', 'keycloakAuth',
      ($routeProvider: che.route.IRouteProvider, $httpProvider: ng.IHttpProvider, keycloakAuth: any) => {

      // add the Keycloak interceptor for Che API
      if (keycloakAuth.isPresent) {
        $httpProvider.interceptors.push('keycloakTokenInterceptor');
      }

      // add the ETag interceptor for Che API
      $httpProvider.interceptors.push('eTagInterceptor');
    }]);
  }

}
