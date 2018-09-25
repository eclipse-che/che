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
import {CheHttpBackend} from '../api/test/che-http-backend';

/**
 * Test the git URL
 * @author Florent Benoit
 */

describe('git-url-validator', function() {
  var $scope, form;
  /**
   * Backend for handling http operations
   */
  var httpBackend;

  /**
   * Che backend
   */
  var cheBackend;

  beforeEach(angular.mock.module('userDashboard'));



  beforeEach(inject(function($compile: ng.ICompileService, $rootScope: ng.IRootScopeService, cheHttpBackend: CheHttpBackend) {
    $scope = $rootScope;
    // setup backend
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
    cheBackend.setup();

    var element = angular.element(
      '<form name="form">' +
      '<input ng-model="model.myURL" name="url" git-url />' +
      '</form>'
    );
    $scope.model = { myURL: null };
    $compile(element)($scope);
    $scope.$digest();
    form = $scope.form;
  }));

  describe('checkUrls', function() {

    it('ssh URLs', function() {
      var validUrls = ['ssh://user@host.xz:port/path/to/repo.git/',
        'ssh://user@host.xz/path/to/repo.git/',
        'ssh://host.xz:port/path/to/repo.git/',
        'ssh://host.xz:port/path/to/repo.git/',
        'ssh://host.xz/path/to/repo.git/',
        'ssh://user@host.xz/path/to/repo.git/',
        'ssh://host.xz/path/to/repo.git/',
        'ssh://user@host.xz/~user/path/to/repo.git/',
        'ssh://host.xz/~user/path/to/repo.git/',
        'ssh://user@host.xz/~/path/to/repo.git',
        'ssh://host.xz/~/path/to/repo.git'
      ];

      validUrls.forEach(function (url: string) {
        form.url.$setViewValue(url);
        expect($scope.model.myURL).toEqual(url);
        expect(form.url.$valid).toBe(true);
      });
    });


    it('gitHUb URL', function() {
      form.url.$setViewValue('https://github.com/eclipse/che-dashboard');
      expect($scope.model.myURL).toEqual('https://github.com/eclipse/che-dashboard');
      expect(form.url.$valid).toBe(true);
    });

    it('bitbucket URL', function() {
      form.url.$setViewValue('https://newuserme@bitbucket.org/newuserme/bb101repo.git');
      expect($scope.model.myURL).toEqual('https://newuserme@bitbucket.org/newuserme/bb101repo.git');
      expect(form.url.$valid).toBe(true);
    });

    it('invalids URLs', function() {
      var invalidUrls = ['eeeee'];
      invalidUrls.forEach(function (url: string) {
        form.url.$setViewValue(url);
        // undefined model
        expect($scope.model.myURL).toBeUndefined();
        // invalid
        expect(form.url.$valid).toBe(false);
        expect(form.url.$invalid).toBe(true);
      });
    });
  });
});
