/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

describe('controllers', function(){
  var scope;

  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject(function($rootScope) {
    scope = $rootScope.$new();
  }));

  it('empty', inject(function($controller) {
    expect(scope.awesomeThings).toBeUndefined();

    $controller('DashboardController', {
      $scope: scope
    });

  }));
});
