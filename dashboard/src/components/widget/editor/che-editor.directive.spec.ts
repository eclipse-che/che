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
import {CheAPIBuilder} from '../../api/builder/che-api-builder.factory';
import {CheHttpBackend} from '../../api/test/che-http-backend';
import {CheEditorController} from './che-editor.controller';

interface ITestScope extends ng.IScope {
  model: any;
}

/**
 * Test of the CheEditor
 * @author Oleksii Orel
 */
describe('CheEditor >', () => {

  let $scope: ITestScope;

  let $compile: ng.ICompileService;

  let $timeout: ng.ITimeoutService;

  let compiledDirective;

  let cheAPIBuilder: CheAPIBuilder;

  let controller: CheEditorController;

  let cheHttpBackend: CheHttpBackend;

  /**
   * Backend for handling http operations
   */
  let $httpBackend: ng.IHttpBackendService;

  const requiredErrorMessage = 'A reference is required.';

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_: ng.ICompileService,
                     _$rootScope_: ng.IRootScopeService,
                     _$timeout_: ng.ITimeoutService,
                     _cheAPIBuilder_: CheAPIBuilder,
                     _cheHttpBackend_: CheHttpBackend) => {
    $scope = _$rootScope_.$new() as ITestScope;
    $timeout = _$timeout_;
    $compile = _$compile_;

    cheAPIBuilder = _cheAPIBuilder_;
    cheHttpBackend = _cheHttpBackend_;

    $httpBackend = _cheHttpBackend_.getHttpBackend();
    // avoid tracking requests from branding controller
    $httpBackend.whenGET(/.*/).respond(200, '');
    $httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  beforeEach(() => {
    $scope.model = {};
    $scope.model.editorScript = 'test';
    $scope.model.editorState = {isValid: true, errors: []};
    $scope.model.editorMode = 'text/x-yaml';
    $scope.model.isRecipeValidFn = () => {
      return {isValid: true, errors: []};
    };
    $scope.model.onChangeFn = () => {
      return;
    };
    compileDirective();
  });

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function compileDirective(): void {
    compiledDirective = $compile(angular.element(
      `<che-editor editor-content="model.editorScript"
                   editor-state="model.editorState"
                   validator="model.isRecipeValidFn()"
                   on-content-change="model.onChangeFn()"
                   editor-mode="model.editorMode"
                   required><div ng-message="required">${requiredErrorMessage}</div></che-editor>`
    ))($scope);
    $scope.$digest();
    controller = compiledDirective.controller('cheEditor');
    $timeout.flush();
  }

  it(`should correctly compile the directive > `, () => {
    expect(compiledDirective).toBeTruthy();
    expect(controller).toBeTruthy();
  });

  describe(`when editor content is changed >`, () => {

    describe(`and content is valid >`, () => {

      it(`should call 'isRecipeValidFn' callback >`, () => {
        spyOn($scope.model, 'isRecipeValidFn');

        const newValue = 'test1';
        expect($scope.model.editorScript).not.toEqual(newValue);

        controller.setEditorValue(newValue);
        $scope.$digest();
        $timeout.flush();

        expect($scope.model.editorScript).toEqual(newValue);
        expect($scope.model.isRecipeValidFn).toHaveBeenCalled();
      });

      it(`should call 'onChangeFn' callback >`, () => {
        spyOn($scope.model, 'onChangeFn');

        const newValue = 'test2';
        expect($scope.model.editorScript).not.toEqual(newValue);

        controller.setEditorValue(newValue);
        $scope.$digest();
        $timeout.flush();

        expect($scope.model.editorScript).toEqual(newValue);
        expect($scope.model.onChangeFn).toHaveBeenCalled();
      });

    });

    describe(`and content isn't valid >`, () => {

      it(`should show a custom validator error message`, () => {
        const errorMessage = 'test error message';
        const newValue = 'test3';
        $scope.model.isRecipeValidFn = () => {
          return {isValid: false, errors: [errorMessage]};
        };
        expect($scope.model.editorScript).not.toEqual(newValue);
        expect($scope.model.editorState.isValid).toEqual(true);

        controller.setEditorValue(newValue);
        $scope.$digest();
        $timeout.flush();

        expect($scope.model.editorScript).toEqual(newValue);
        expect($scope.model.editorState.isValid).toEqual(false);
        const errorMessagesElement = compiledDirective.find('.validator-checks');
        expect(errorMessagesElement.html()).toContain(errorMessage);
      });

      it(`should show transclude error message if editor is required`, () => {
        const newValue = '';
        expect($scope.model.editorScript).not.toEqual(newValue);
        expect($scope.model.editorState.isValid).toEqual(true);

        controller.setEditorValue(newValue);
        $scope.$digest();
        $timeout.flush();

        expect($scope.model.editorScript).toEqual(newValue);
        expect($scope.model.editorState.isValid).toEqual(true);
        const errorMessagesElement = compiledDirective.find('.custom-checks');
        expect(errorMessagesElement.html()).toContain(requiredErrorMessage);
      });

    });

  });

});
