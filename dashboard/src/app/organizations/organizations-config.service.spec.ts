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
import { CheAPIBuilder } from '../../components/api/builder/che-api-builder.factory';
import { CheHttpBackend } from '../../components/api/test/che-http-backend';
import { OrganizationsConfigServiceMock } from './organizations-config.service.mock';
import { CheBranding } from '../../components/branding/che-branding';

/* tslint:disable:no-empty */
describe('OrganizationsConfig >', () => {

  let $route;

  let $injector: ng.auto.IInjectorService;

  let $rootScope;

  let $httpBackend;

  let mock;

  let setPath: (path: string) => void;

  /**
   * Setup module
   */

  beforeEach(() => {
    angular.mock.module('userDashboard');
  });

  /**
   * Inject service and http backend
   */
  beforeEach(inject((
    _$injector_: ng.auto.IInjectorService,
    _$location_: ng.ILocationService,
    _$route_: ng.route.IRouteService,
    _$rootScope_: ng.IRootScopeService,
    _cheHttpBackend_: CheHttpBackend,
    _cheAPIBuilder_: CheAPIBuilder
  ) => {
    $injector = _$injector_;
    $route = _$route_;
    $rootScope = _$rootScope_;
    $httpBackend = _cheHttpBackend_.getHttpBackend();

    setPath = path => {
      _$location_.path(path);
      _$rootScope_.$digest();
    };

    mock = new OrganizationsConfigServiceMock(_cheAPIBuilder_, _cheHttpBackend_);
    mock.mockData();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    $httpBackend.verifyNoOutstandingRequest();
    $httpBackend.verifyNoOutstandingExpectation();
  });

  describe('create root organization "/admin/create-organization" >', () => {

    it('should resolve route and return data', () => {

      const route = $route.routes['/admin/create-organization'];
      const resolveBlock = route.resolve.initData;

      // stub functions
      const callbacks = {
        testResolve: (...args: any[]) => { },
        testReject: (...args: any[]) => { }
      };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      setPath('/admin/create-organization');

      const service = $injector.invoke(resolveBlock);

      service
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $rootScope.$digest();
      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalledWith({
        parentQualifiedName: '',
        parentOrganizationId: '',
        parentOrganizationMembers: []
      });
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

  });

  describe('create sub-organization "/admin/create-organization/{parentId}" >', () => {

    it('should resolve route and return data', () => {
      const organizations = mock.getOrganizations();
      const parentOrg = organizations[0];
      const users = mock.getUsersByOrganizationId(parentOrg.id);
      const buildIdsList = (res: string[], user: che.IUser) => {
        res.push(user.id);
        return res;
      };

      const route = $route.routes['/admin/create-organization/:parentQualifiedName*'];
      const resolveBlock = route.resolve.initData;

      // stub functions
      const callbacks = {
        testResolve: (data: any) => {
          expect(data.parentQualifiedName).toEqual(parentOrg.qualifiedName);
          expect(data.parentOrganizationId).toEqual(parentOrg.id);
          expect(data.parentOrganizationMembers.length).toEqual(users.length);

          const parentMemberIds = data.parentOrganizationMembers.reduce(buildIdsList, []).sort();
          const userIds = users.reduce(buildIdsList, []).sort();
          expect(parentMemberIds).toEqual(userIds);
        },
        testReject: () => { }
      };

      // create spies
      spyOn(callbacks, 'testResolve').and.callThrough();
      spyOn(callbacks, 'testReject');

      setPath(`/admin/create-organization/${parentOrg.qualifiedName}`);

      const service = $injector.invoke(resolveBlock);

      service
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $rootScope.$digest();
      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

    it('should resolve route and return data if organizations request fails', () => {
      const organizations = mock.getOrganizations();
      const parentOrg = organizations[0];

      const route = $route.routes['/admin/create-organization/:parentQualifiedName*'];
      const resolveBlock = route.resolve.initData;

      // stub functions
      const callbacks = {
        testResolve: (...args: any[]) => { },
        testReject: (...args: any[]) => { }
      };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      setPath(`/admin/create-organization/${parentOrg.qualifiedName}`);

      // make response for organizations list fail
      $httpBackend.expect('GET', /\/api\/organization(\?.*$)?/).respond(500, [], {message: 'response failed'});

      const service = $injector.invoke(resolveBlock);

      service
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $rootScope.$digest();
      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalledWith({
        parentQualifiedName: parentOrg.qualifiedName,
        parentOrganizationId: '',
        parentOrganizationMembers: []
      });
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

    it('should resolve route and return data if parent organization is not found', () => {
      const fakeQualifiedName = 'fake/qualified/name';

      const route = $route.routes['/admin/create-organization/:parentQualifiedName*'];
      const resolveBlock = route.resolve.initData;

      // stub functions
      const callbacks = {
        testResolve: (...args: any[]) => { },
        testReject: (...args: any[]) => { }
      };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      setPath(`/admin/create-organization/${fakeQualifiedName}`);

      const service = $injector.invoke(resolveBlock);

      service
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalledWith({
        parentQualifiedName: fakeQualifiedName,
        parentOrganizationId: '',
        parentOrganizationMembers: []
      });
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

  });

  describe('get root organization details "/organization/{rootOrganization}" >', () => {

    it('should resolve route and return data', () => {
      const organizations = mock.getOrganizations();
      const parentOrg = organizations[0];

      const route = $route.routes['/organization/:organizationName*'];
      const resolveBlock = route.resolve.initData;

      // stub functions
      const callbacks = {
        testResolve: (initData: any) => {
          // this is necessary because of different types
          const equal = angular.equals(initData.organization, parentOrg);
          expect(equal).toBeTruthy();

          expect(initData.parentOrganizationMembers).toEqual([]);
        },
        testReject: () => { }
      };

      // create spies
      spyOn(callbacks, 'testResolve').and.callThrough();
      spyOn(callbacks, 'testReject');

      setPath(`/organization/${parentOrg.qualifiedName}`);

      const service = $injector.invoke(resolveBlock);

      service
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $rootScope.$digest();
      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

    it('should resolve route and return data if request for organizations list fails', () => {
      const organizations = mock.getOrganizations();
      const parentOrg = organizations[0];

      const route = $route.routes['/organization/:organizationName*'];
      const resolveBlock = route.resolve.initData;

      // stub functions
      const callbacks = {
        testResolve: (initData: any) => {
          expect(initData.organization).toBeFalsy();
          expect(initData.parentOrganizationMembers).toEqual([]);
        },
        testReject: () => { }
      };

      // create spies
      spyOn(callbacks, 'testResolve').and.callThrough();
      spyOn(callbacks, 'testReject');

      setPath(`/organization/${parentOrg.qualifiedName}`);

      // make response for organizations list fail
      $httpBackend.expect('GET', /\/api\/organization(\?.*$)?/).respond(500, [], {message: 'response failed'});

      const service = $injector.invoke(resolveBlock);

      service
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $rootScope.$digest();
      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

  });

  describe('get sub-organization details "/organization/{rootOrganization}/{sub-organization}" >', () => {

    it('should resolve route and return data', () => {
      const organizations = mock.getOrganizations();
      // get sub-organization
      const subOrg = organizations[1];

      // get parent organization users
      const users = mock.getUsersByOrganizationId(subOrg.parent);
      const buildIdsList = (res: string[], user: che.IUser) => {
        res.push(user.id);
        return res;
      };

      const route = $route.routes['/organization/:organizationName*'];
      const resolveBlock = route.resolve.initData;

      // stub functions
      const callbacks = {
        testResolve: (initData: any) => {
          // this is necessary because of different types
          const organizationsAreEqual = angular.equals(initData.organization, subOrg);
          expect(organizationsAreEqual).toBeTruthy();

          expect(initData.parentOrganizationMembers.length).toEqual(users.length);

          const parentMemberIds = initData.parentOrganizationMembers.reduce(buildIdsList, []).sort();
          const userIds = users.reduce(buildIdsList, []).sort();
          expect(parentMemberIds).toEqual(userIds);
        },
        testReject: () => { }
      };

      // create spies
      spyOn(callbacks, 'testResolve').and.callThrough();
      spyOn(callbacks, 'testReject');

      setPath(`/organization/${subOrg.qualifiedName}`);

      const service = $injector.invoke(resolveBlock);

      service
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $rootScope.$digest();
      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

    it('should resolve route and return data if request for organizations list fails', () => {
      const organizations = mock.getOrganizations();
      const subOrg = organizations[1];

      const route = $route.routes['/organization/:organizationName*'];
      const resolveBlock = route.resolve.initData;

      // stub functions
      const callbacks = {
        testResolve: (initData: any) => {
          expect(initData.organization).toBeFalsy();
          expect(initData.parentOrganizationMembers).toEqual([]);
        },
        testReject: () => { }
      };

      // create spies
      spyOn(callbacks, 'testResolve').and.callThrough();
      spyOn(callbacks, 'testReject');

      setPath(`/organization/${subOrg.qualifiedName}`);

      // make response for organizations list fail
      $httpBackend.expect('GET', /\/api\/organization(\?.*$)?/).respond(500, [], { message: 'response failed' });

      const service = $injector.invoke(resolveBlock);

      service
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $rootScope.$digest();
      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

  });

});
