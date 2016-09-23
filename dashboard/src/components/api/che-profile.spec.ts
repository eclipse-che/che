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

/**
 * Test of the CheProfile
 */
describe('CheProfile', function () {

  /**
   * Profile Factory for the test
   */
  var factory;

  /**
   * API builder.
   */
  var apiBuilder;

  /**
   * Backend for handling http operations
   */
  var httpBackend;

  /**
   * che backend
   */
  var cheBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject(function (cheProfile, cheAPIBuilder, cheHttpBackend) {
    factory = cheProfile;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(function () {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  /**
   * Check that we're able to fetch profile
   */
  it('Fetch profile', function () {
      // setup tests objects
      var profileId = 'idDefaultUser';
      var email = 'eclipseChe@eclipse.org';
      var firstName = 'FirstName';
      var lastName = 'LastName';

      var defaultProfile = apiBuilder.getProfileBuilder().withId(profileId).withEmail(email).withFirstName(firstName).withLastName(lastName).build();

      // providing request
      // add defaultProfile on Http backend
      cheBackend.addDefaultProfile(defaultProfile);

      // setup backend
      cheBackend.setup();

      // fetch profile
      factory.fetchProfile();

      // expecting GETs
      httpBackend.expectGET('/api/profile');
      // flush command
      httpBackend.flush();

      // now, check profile
      var profile = factory.getProfile();

      // check id, email, firstName and lastName in profile attributes
      expect(profile.id).toEqual(profileId);
      expect(profile.email).toEqual(email);
      expect(profile.attributes.firstName).toEqual(firstName);
      expect(profile.attributes.lastName).toEqual(lastName);
    }
  );

  /**
   * Check that we're able to set attributes into profile
   */
  it('Set attributes', function () {
      // setup tests object
      var testAttributes = {lastName: '<none>', email: 'eclipseChe@eclipse.org'};

      // setup backend
      cheBackend.setup();
      cheBackend.setAttributes(testAttributes);

      // fetch profile
      factory.setAttributes(testAttributes);

      // expecting a PUT
      httpBackend.expectPUT('/api/profile/attributes');

      // flush command
      httpBackend.flush();

      // now, check profile
      var profile = factory.getProfile();

      // check profile new attributes
      expect(profile.attributes).toEqual(testAttributes);

    }
  );

});
