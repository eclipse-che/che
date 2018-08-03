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
import {CheFactory} from '../api/che-factory.factory';
import {CheAPIBuilder} from '../api/builder/che-api-builder.factory';
import {CheHttpBackend} from '../api/test/che-http-backend';

interface ITestRootScope extends ng.IRootScopeService {
  factoryName: string;
  myForm: ng.IFormController;
}

interface ITestFormController extends ng.IFormController {
  name: ng.INgModelController;
}

/**
 * Test the factory name uniqueness
 * @author Oleksii Kurinnyi
 */

describe('unique-factory-name-validator >', function() {
  let $rootScope: ITestRootScope,
    $compile: ng.ICompileService,
    myForm: ITestFormController;

  /**
   * Factory API
   */
  let cheFactory: CheFactory;

  /**
   * API builder.
   */
  let cheAPIBuilder: CheAPIBuilder;

  /**
   * Che backend
   */
  let cheHttpBackend: CheHttpBackend;

  /**
   * Backend for handling http operations
   */
  let $httpBackend: ng.IHttpBackendService;

  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_: ng.ICompileService,
                     _$rootScope_: ITestRootScope,
                     _cheFactory_: CheFactory,
                     _cheAPIBuilder_: CheAPIBuilder,
                     _cheHttpBackend_: CheHttpBackend) => {
    $rootScope = _$rootScope_;
    $compile = _$compile_;
    cheFactory = _cheFactory_;
    cheAPIBuilder = _cheAPIBuilder_;
    cheHttpBackend = _cheHttpBackend_;
    $httpBackend = _cheHttpBackend_.getHttpBackend();

  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  describe('validate factory name >', function() {
    const factoryName1 = 'factoryName1',
      loadedFactoryName = factoryName1,
      factoryName2 = 'factoryName2',
      uniqueName = 'uniqueName',
      userId = 'userId';

    beforeEach(() => {
      // setup default user
      const user = cheAPIBuilder.getUserBuilder().withId(userId).build();
      cheHttpBackend.setDefaultUser(user);

      cheHttpBackend.usersBackendSetup();

      // setup factories
      const factoryId1 = 'factoryId1',
        factory1 = cheAPIBuilder.getFactoryBuilder().withId(factoryId1).withName(factoryName1).build();
      cheHttpBackend.addUserFactory(factory1);

      const factoryId2 = 'factoryId2',
        factory2 = cheAPIBuilder.getFactoryBuilder().withId(factoryId2).withName(factoryName2).build();
      cheHttpBackend.addUserFactory(factory2);

      cheHttpBackend.factoriesBackendSetup();

      cheHttpBackend.setup();

      // setup backend
      $httpBackend.whenGET( `/api/factory/find?creator.userId=${userId}&name=${uniqueName}`).respond(404, {error: 'not found'});

      // fetch first factory
      cheFactory.fetchFactoryByName(factoryName1, userId);

      // flush HTTP backend
      $httpBackend.flush();
    });

    describe('initially factory has an unique name >', () => {

      beforeEach(() => {
        $rootScope.factoryName = uniqueName;

        const element = angular.element(`
<form name="myForm">
  <input ng-model="factoryName" name="name" unique-factory-name="uniqueFactoryName" />
</form>
`);
        $compile(element)($rootScope);
        myForm = $rootScope.myForm as ITestFormController;
      });

      it('the form should be valid >', () => {
        // flush HTTP backend
        $httpBackend.flush();

        // check form (expect valid)
        expect(myForm.name.$invalid).toBe(false);
        expect(myForm.name.$valid).toBe(true);
      });

      it('the HTTP request should be made >', () => {
        $httpBackend.expectGET(`/api/factory/find?creator.userId=${userId}&name=${uniqueName}`);

        // flush HTTP backend
        $httpBackend.flush();
      });

      describe('change name to non-unique, factory is already downloaded >', () => {

        beforeEach(() => {
          myForm.name.$setViewValue(loadedFactoryName);
        });

        it ('the form should be invalid >', () => {
          // check form (expect invalid)
          expect(myForm.name.$invalid).toBe(true);
          expect(myForm.name.$valid).toBe(false);
        });

        it('the HTTP request should not be made >', () => {
          spyOn(cheFactory, 'fetchFactoryByName');
          expect(cheFactory.fetchFactoryByName).not.toHaveBeenCalled();
        });

      });

      describe('change name to non-unique, factory is not downloaded yet >', () => {

        it ('the form should be invalid >', () => {
          myForm.name.$setViewValue(factoryName2);

          // flush HTTP backend
          $httpBackend.flush();

          // check form (expect invalid)
          expect(myForm.name.$invalid).toBe(true);
          expect(myForm.name.$valid).toBe(false);
        });

        it('the HTTP request should be made >', () => {
          $httpBackend.expectGET(`/api/factory/find?creator.userId=${userId}&name=${factoryName2}`);

          myForm.name.$setViewValue(factoryName2);

          // flush HTTP backend
          $httpBackend.flush();
        });

      });

    });

    describe('initially factory has a non-unique name, factory is already downloaded >', () => {

      beforeEach(() => {
        $rootScope.factoryName = loadedFactoryName;

        const element = angular.element(`
<form name="myForm">
  <input ng-model="factoryName" name="name" unique-factory-name="uniqueFactoryName" />
</form>
`);
        $compile(element)($rootScope);
        myForm = $rootScope.myForm as ITestFormController;

        $rootScope.$digest();
      });

      it('the form should be invalid >', () => {
        // check form (expect invalid)
        expect(myForm.name.$invalid).toBe(true);
        expect(myForm.name.$valid).toBe(false);
      });

      it('the HTTP request should not be made >', () => {
        spyOn(cheFactory, 'fetchFactoryByName');
        expect(cheFactory.fetchFactoryByName).not.toHaveBeenCalled();
      });

      describe('change name to unique> ', () => {

        it ('the form should be valid >', () => {
          myForm.name.$setViewValue(uniqueName);

          // flush HTTP backend
          $httpBackend.flush();

          // check form (expect valid)
          expect(myForm.name.$invalid).toBe(false);
          expect(myForm.name.$valid).toBe(true);
        });

        it('the HTTP request should be made >', () => {
          $httpBackend.expectGET(`/api/factory/find?creator.userId=${userId}&name=${uniqueName}`);

          myForm.name.$setViewValue(uniqueName);

          // flush HTTP backend
          $httpBackend.flush();
        });

      });

    });

    describe('initially factory has a non-unique name, factory is not downloaded yet >', () => {

      beforeEach(() => {
        $rootScope.factoryName = factoryName2;

        const element = angular.element(`
<form name="myForm">
  <input ng-model="factoryName" name="name" unique-factory-name="uniqueFactoryName" />
</form>
`);
        $compile(element)($rootScope);
        myForm = $rootScope.myForm as ITestFormController;
      });

      it('the form should be invalid >', () => {
        // flush HTTP backend
        $httpBackend.flush();

        // check form (expect invalid)
        expect(myForm.name.$invalid).toBe(true);
        expect(myForm.name.$valid).toBe(false);
      });

      it('the HTTP request should be made >', () => {
        $httpBackend.expectGET(`/api/factory/find?creator.userId=${userId}&name=${factoryName2}`);

        // flush HTTP backend
        $httpBackend.flush();
      });

      describe('change name to unique> ', () => {

        it ('the form should be valid >', () => {
          myForm.name.$setViewValue(uniqueName);

          // flush HTTP backend
          $httpBackend.flush();

          // check form (expect valid)
          expect(myForm.name.$invalid).toBe(false);
          expect(myForm.name.$valid).toBe(true);
        });

        it('the HTTP request should be made >', () => {
          $httpBackend.expectGET(`/api/factory/find?creator.userId=${userId}&name=${uniqueName}`);

          myForm.name.$setViewValue(uniqueName);

          // flush HTTP backend
          $httpBackend.flush();
        });

      });

    });

  });

});
