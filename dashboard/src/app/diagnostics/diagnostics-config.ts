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


import {Diagnostics} from './diagnostics.directive';
import {DiagnosticsController} from './diagnostics.controller';
import {DiagnosticsWebsocketWsMaster} from './test/diagnostics-websocket-wsmaster.factory';
import {DiagnosticsWorkspaceStartCheck} from './test/diagnostics-workspace-start-check.factory';
import {DiagnosticsRunningWorkspaceCheck} from './test/diagnostics-workspace-check-workspace.factory';

/**
 * Diagnostics configuration
 * @author Florent Benoit
 */
export class DiagnosticsConfig {

  constructor(register: che.IRegisterService) {

    register.factory('diagnosticsWebsocketWsMaster', DiagnosticsWebsocketWsMaster);
    register.factory('diagnosticsWorkspaceStartCheck', DiagnosticsWorkspaceStartCheck);
    register.factory('diagnosticsRunningWorkspaceCheck', DiagnosticsRunningWorkspaceCheck);

    register.directive('diagnostics', Diagnostics);
    register.controller('DiagnosticsController', DiagnosticsController);

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/diagnostic', {
        title: 'Diagnostic',
        templateUrl: 'app/diagnostics/diagnostics.html'
      });
    }]);

  }
}
