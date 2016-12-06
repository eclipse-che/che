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
import {CheHttpBackend} from '../../../../../components/api/test/che-http-backend';

/**
 * Test of the WorkspaceRecipeImport
 * @author Oleksii Kurinnyi
 */
describe('WorkspaceRecipeImport', () => {

  let $rootScope, $compile, compiledDirective;

  /**
   * Backend for handling http operations
   */
  let httpBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_: ng.ICompileService,
                     _$rootScope_: ng.IRootScopeService,
                     cheHttpBackend: CheHttpBackend) => {
    $rootScope = _$rootScope_.$new();
    $compile = _$compile_;

    httpBackend = cheHttpBackend.getHttpBackend();
    httpBackend.whenGET(/.*/).respond(200, '');

    $rootScope.model = {
      recipeUrl: '',
      recipeFormat: 'compose'
    };
  }));

  function getCompiledElement() {
    let element = $compile(angular.element('<che-workspace-recipe-import che-recipe-url="model.recipeUrl" che-recipe-format="model.recipeFormat"></che-workspace-recipe-import>'))($rootScope);
    $rootScope.$digest();
    return element;
  }

  describe('should pass', () => {

    beforeEach(() => {
      compiledDirective = getCompiledElement();
    });

    it('correct HTTP URL', () => {
      $rootScope.model.recipeUrl = 'http://host.com/config';
      $rootScope.$digest();
      expect(compiledDirective.html()).not.toContain('URL is not valid.');
    });

    it('correct HTTP URL with port, query and fragment', () => {
      $rootScope.model.recipeUrl = 'http://host.com:1234/path?query=abc#fragment';
      $rootScope.$digest();
      expect(compiledDirective.html()).not.toContain('URL is not valid.');
    });

    it('correct HTTPS URL', () => {
      $rootScope.model.recipeUrl = 'https://host.com/config';
      $rootScope.$digest();
      expect(compiledDirective.html()).not.toContain('URL is not valid.');
    });

    it('correct FTP URL', () => {
      $rootScope.model.recipeUrl = 'ftp://host.com/config';
      $rootScope.$digest();
      expect(compiledDirective.html()).not.toContain('URL is not valid.');
    });

  });

  describe('should fail', () => {

    beforeEach(() => {
      compiledDirective = getCompiledElement();
    });

    it('URL with incorrect scheme', () => {
      $rootScope.model.recipeUrl = 'abc://host.com:1234/config';
      $rootScope.$digest();
      expect(compiledDirective.html()).toContain('URL is not valid.');
    });

    it('URL with no port entered', () => {
      $rootScope.model.recipeUrl = 'http://host.com:/config';
      $rootScope.$digest();
      expect(compiledDirective.html()).toContain('URL is not valid.');
    });

    it('URL with incorrect port', () => {
      $rootScope.model.recipeUrl = 'http://host.com:12ab/config';
      $rootScope.$digest();
      expect(compiledDirective.html()).toContain('URL is not valid.');
    });

  });

});
