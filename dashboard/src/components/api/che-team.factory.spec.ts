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
import {CheTeam} from './che-team.factory';
import {CheUser} from './che-user.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheHttpBackend} from './test/che-http-backend';

/* tslint:disable:no-empty */

/**
 * Test of the Che Team API
 */
describe('CheTeam >', () => {

  /**
   * User Factory for the test
   */
  let cheTeam;

  /**
   * Che User API
   */
  let cheUser;

  /**
   * che API builder.
   */
  let cheAPIBuilder;

  /**
   * Che backend
   */
  let cheHttpBackend;

  /**
   * Backend for handling http operations
   */
  let $httpBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject((
    _cheTeam_: CheTeam,
    _cheUser_: CheUser,
    _cheAPIBuilder_: CheAPIBuilder,
    _cheHttpBackend_: CheHttpBackend
  ) => {
    cheTeam = _cheTeam_;
    cheUser = _cheUser_;
    cheAPIBuilder = _cheAPIBuilder_;
    cheHttpBackend = _cheHttpBackend_;
    $httpBackend = cheHttpBackend.getHttpBackend();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    $httpBackend.verifyNoOutstandingRequest();
    $httpBackend.verifyNoOutstandingExpectation();
  });

  /**
   * Check than we are able to fetch team data
   */
  describe('Fetch team method >', () => {

    beforeEach(() => {
      cheHttpBackend.setup();

      /* user setup */

      // setup tests objects
      const userId = 'idTestUser';
      const email = 'eclipseChe@eclipse.org';

      const testUser = cheAPIBuilder.getUserBuilder().withId(userId).withEmail(email).build();

      // providing request
      // add test user on Http backend
      cheHttpBackend.setDefaultUser(testUser);

      // setup backend for users
      cheHttpBackend.usersBackendSetup();

      /* team setup */

      // setup tests objects
      const teamId = 'idTestTeam';
      const teamName = 'testTeam';

      const testTeam = cheAPIBuilder.getTeamBuilder().withId(teamId).withName(teamName).build();

      // add test team on Http backend
      cheHttpBackend.addTeamById(testTeam);

      // setup backend for teams
      cheHttpBackend.teamsBackendSetup();
    });

    it('should reject promise if team\'s request failed', () => {
      /* fulfil all requests */
      $httpBackend.flush();

      const errorMessage = 'teams request failed',
            callbacks = {
              testResolve: () => { },
              testReject: (error: any) => {
                expect(error.data.message).toEqual(errorMessage);
              }
            };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      // change response to make request fail
      $httpBackend.expectGET(/\/api\/organization(\?.*$)?/).respond(404, {message: errorMessage});

      cheTeam.fetchTeams()
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $httpBackend.flush();

      expect(callbacks.testResolve).not.toHaveBeenCalled();
      expect(callbacks.testReject).toHaveBeenCalled();
    });

    it('should resolve promise', () => {
      /* fulfil all requests */
      $httpBackend.flush();

      const errorMessage = 'user request failed',
            callbacks = {
              testResolve: () => { },
              testReject: (error: any) => {
                expect(error.data.message).toEqual(errorMessage);
              }
            };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      cheTeam.fetchTeams()
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

    it('should resolve promise if team\'s request status code equals 304', () => {
      /* fulfil all requests */
      $httpBackend.flush();

      const errorMessage = 'teams request failed',
            callbacks = {
              testResolve: () => { },
              testReject: (error: any) => {
                expect(error.data.message).toEqual(errorMessage);
              }
            };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      cheTeam.fetchTeams()
        .then(callbacks.testResolve)
        .catch(callbacks.testReject);

      // change response
      $httpBackend.expect('GET', /\/api\/organization(\?.*$)?/).respond(304, {});

      $httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

  });

});
