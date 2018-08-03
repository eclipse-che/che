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

import {CheAPIBuilder} from '../../../../../components/api/builder/che-api-builder.factory';
import {CheHttpBackend} from '../../../../../components/api/test/che-http-backend';
import {RecipeEditorController} from './recipe-editor.controller';

interface ITestScope extends ng.IScope {
  model: any;
}

/**
 * Test of the RecipeEditorDirective
 * @author Oleksii Kurinnyi
 */
describe('RecipeEditorDirective >', () => {

  let $scope: ITestScope;

  let $compile: ng.ICompileService;

  let $timeout: ng.ITimeoutService;

  let compiledDirective;

  let cheAPIBuilder: CheAPIBuilder;

  let controller: RecipeEditorController;

  let cheHttpBackend: CheHttpBackend;

  /**
   * Backend for handling http operations
   */
  let $httpBackend: ng.IHttpBackendService;

  const validRecipeContent = 'new recipe content';
  const invalidRecipeContent = 'invalid recipe';
  const errorMessage = `This recipe isn't valid.`;

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
    $scope.model.allowInvalidRecipe = true;
    $scope.model.editorMode = '';
    $scope.model.changeRecipeFn = () => { return; };
    $scope.model.validateRecipeFn = (recipe: string) => {
      if (recipe === invalidRecipeContent) {
        return errorMessage;
      }
      return null;
    };
    compileDirective();
  });

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function compileDirective(): void {
    compiledDirective = $compile(angular.element(
      `<recipe-editor allow-invalid-recipe="model.allowInvalidRecipe"
                      editor-mode="model.editorMode"
                      change-recipe-fn="model.changeRecipeFn(content)"
                      validate-recipe-fn="model.validateRecipeFn(content)"></recipe-editor>`
    ))($scope);
    $scope.$digest();
    controller = compiledDirective.controller('recipeEditor');
    $timeout.flush();
  }

  it(`should correctly compile the directive > `, () => {
    expect(compiledDirective).toBeTruthy();
    expect(controller).toBeTruthy();
  });

  describe(`when editor content is changed >`, () => {

    describe(`and content is valid >`, () => {

      it(`should call 'validateRecipeFn' callback >`, () => {
        spyOn($scope.model, 'validateRecipeFn');

        controller.trackChangesInProgress(validRecipeContent);
        $scope.$digest();
        $timeout.flush();

        expect($scope.model.validateRecipeFn).toHaveBeenCalledWith(validRecipeContent);
      });

      it(`should call 'changeRecipeFn' callback >`, () => {
        spyOn($scope.model, 'changeRecipeFn');

        controller.trackChangesInProgress(validRecipeContent);
        $timeout.flush();

        expect($scope.model.changeRecipeFn).toHaveBeenCalledWith(validRecipeContent);
      });

    });

    describe(`and content isn't valid >`, () => {

      it(`should call 'validateRecipeFn' callback >`, () => {
        spyOn($scope.model, 'validateRecipeFn');

        controller.trackChangesInProgress(invalidRecipeContent);
        $scope.$digest();
        $timeout.flush();

        expect($scope.model.validateRecipeFn).toHaveBeenCalledWith(invalidRecipeContent);
      });

      it(`should call 'changeRecipeFn' with a recipe content if 'allowInvalidRecipe' is true >`, () => {
        $scope.model.allowInvalidRecipe = true;

        compileDirective();

        spyOn($scope.model, 'changeRecipeFn');

        controller.trackChangesInProgress(invalidRecipeContent);
        $timeout.flush();

        expect($scope.model.changeRecipeFn).toHaveBeenCalledWith(invalidRecipeContent);
      });

      it(`should call 'changeRecipeFn' with 'null' if 'allowInvalidRecipe' is false`, () => {
        $scope.model.allowInvalidRecipe = false;

        compileDirective();

        spyOn($scope.model, 'changeRecipeFn');

        controller.trackChangesInProgress(invalidRecipeContent);
        $timeout.flush();

        expect($scope.model.changeRecipeFn).toHaveBeenCalledWith(null);
      });

      it(`should not show error message if form is pristine`, () => {
        controller.isFormDirty = false;
        controller.trackChangesInProgress(invalidRecipeContent);
        $scope.$digest();
        $timeout.flush();
        $scope.$digest();

        const errorMessagesElement = compiledDirective.find('.error-message');
        expect(errorMessagesElement.find('div').length).toEqual(0);
      });

      it(`should show error message if form is dirty`, () => {
        controller.isFormDirty = true;
        controller.trackChangesInProgress(invalidRecipeContent);
        $scope.$digest();
        $timeout.flush();
        $scope.$digest();

        const errorMessagesElement = compiledDirective.find('.error-message');
        expect(errorMessagesElement.find('div').length).not.toEqual(0);
        expect(errorMessagesElement.html()).toContain(errorMessage);
      });

    });

  });

});
