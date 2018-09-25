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
import {CheProfile} from './che-profile.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheHttpBackend} from './test/che-http-backend';

/**
 * Test of the CheProfile
 */
describe('CheProfile', () => {
  /**
   * Profile Factory for the test
   */
  let factory: CheProfile;
  /**
   * API builder.
   */
  let apiBuilder: CheAPIBuilder;
  /**
   * Backend for handling http operations
   */
  let httpBackend: ng.IHttpBackendService;
  /**
   * che backend
   */
  let cheBackend: CheHttpBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject((cheProfile: CheProfile, cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend) => {
    factory = cheProfile;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  /**
   * Check that we're able to fetch profile
   */
  it('Fetch profile', () => {
      const testProfile = apiBuilder.getProfileBuilder()
        .withId('idDefaultUser')
        .withEmail('eclipseChe@eclipse.org')
        .withFirstName('FirstName')
        .withLastName('LastName')
        .build();

      // setup backend
      cheBackend.addDefaultProfile(testProfile);
      cheBackend.setup();

      factory.fetchProfile();

      httpBackend.expectGET('/api/profile');

      httpBackend.flush();

      const profile = factory.getProfile();

      expect(profile).toEqual(profile);
    }
  );

  /**
   * Check that we're able to set attributes into profile
   */
  it('Set current user attributes', () => {
      const testProfile = apiBuilder.getProfileBuilder()
        .withEmail('test@test.com')
        .withFirstName('testName')
        .build();

      // setup backend
      cheBackend.setAttributes(testProfile.attributes);
      cheBackend.setup();

      factory.setAttributes(testProfile.attributes);

      httpBackend.expectPUT('/api/profile/attributes');

      httpBackend.flush();
    }
  );

  it('Set attributes for the user by Id', () => {
      const testProfile = apiBuilder.getProfileBuilder()
        .withId('testId')
        .withEmail('test@test.com')
        .withFirstName('testName')
        .build();

      // setup backend
      cheBackend.setAttributes(testProfile.attributes, testProfile.userId);
      cheBackend.setup();

      factory.setAttributes(testProfile.attributes, testProfile.userId);

      httpBackend.expectPUT(`/api/profile/${testProfile.userId}/attributes`);

      httpBackend.flush();

      const profile = factory.getProfileById(testProfile.userId);

      expect(profile.attributes).toEqual(testProfile.attributes);
    }
  );

});
