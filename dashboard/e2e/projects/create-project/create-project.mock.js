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

exports.noWorkspaces = function () {

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', ($httpBackend, cheAPIBuilder, cheHttpBackendProvider) => {

      let cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      $httpBackend.whenGET('/api/project-template/all').passThrough();
      $httpBackend.whenGET('/api/stack?maxItems=50').passThrough();

      cheBackend.setup();

    }]);

};
