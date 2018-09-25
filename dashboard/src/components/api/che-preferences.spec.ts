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
import {ChePreferences} from './che-preferences.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheHttpBackend} from './test/che-http-backend';

/**
 * Test of the ChePreferences
 */
describe('ChePreferences', function () {

  /**
   * Preferences Factory for the test
   */
  let factory;

  /**
   * API builder.
   */
  let apiBuilder;

  /**
   * Backend for handling http operations
   */
  let httpBackend;

  /**
   * che backend
   */
  let cheBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject(function (chePreferences: ChePreferences,
                              cheAPIBuilder: CheAPIBuilder,
                              cheHttpBackend: CheHttpBackend) {
    factory = chePreferences;
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
   * Check that we're able to fetch preferences
   */
  it('Fetch preferences', function () {

      // {testURL: {username: 'testName',  password: 'testPassword'}} converted to base64
      let dockerCredentials = 'eyJ0ZXN0VVJMIjp7InVzZXJuYW1lIjoidGVzdE5hbWUiLCJwYXNzd29yZCI6InRlc3RQYXNzd29yZCJ9fQ==';
      let defaultPreferences = {
        pref1: 'value1',
        pref2: 'value2',
        dockerCredentials: dockerCredentials
      };
      // add preferences on Http backend
      cheBackend.addDefaultPreferences(defaultPreferences);

      // setup backend
      cheBackend.setup();

      // fetch preferences
      factory.fetchPreferences();

      // expecting GETs
      httpBackend.expectGET('/api/preferences');

      // flush command
      httpBackend.flush();

      // now, check preferences and decoded registries
      let preferences = factory.getPreferences();
      let registries = factory.getRegistries();

      expect(preferences.pref1).toEqual('value1');
      expect(preferences.pref2).toEqual('value2');
      expect(preferences.dockerCredentials).toEqual(dockerCredentials);
      expect(registries[0].url).toEqual('testURL');
      expect(registries[0].username).toEqual('testName');
      expect(registries[0].password).toEqual('testPassword');
    }
  );

  /**
   * Check that we're able to add docker registries
   */
  it('Add docker registry', function () {
      let defaultPreferences = {pref1: 'value1'};

      let registryUrl = 'testURL';
      let userName = 'testName';
      let userPassword = 'testPassword';
      // {testURL: {username: 'testName',  password: 'testPassword'}} converted to base64
      let dockerCredentials = 'eyJ0ZXN0VVJMIjp7InVzZXJuYW1lIjoidGVzdE5hbWUiLCJwYXNzd29yZCI6InRlc3RQYXNzd29yZCJ9fQ==';

      // setup backend
      cheBackend.setup();
      cheBackend.setPreferences(defaultPreferences);

      // set default preferences
      factory._setPreferences(defaultPreferences);
      // add registry
      factory.addRegistry(registryUrl, userName, userPassword);

      // expecting POST
      httpBackend.expectPOST('/api/preferences');

      // flush command
      httpBackend.flush();

      // now, check default preferences and decoded registries
      let preferences = factory.getPreferences();
      let registries = factory.getRegistries();

      expect(preferences.pref1).toEqual('value1');
      expect(preferences.dockerCredentials).toEqual(dockerCredentials);
      expect(registries[0].url).toEqual(registryUrl);
      expect(registries[0].username).toEqual(userName);
      expect(registries[0].password).toEqual(userPassword);
    }
  );

  /**
   * Check that we're able to update preferences
   */
  it('Update preferences', function () {
      let defaultPreferences = {pref1: 'value1'};
      let newPreferences = {pref2: 'value2'};

      // setup backend
      cheBackend.setup();
      cheBackend.setPreferences(defaultPreferences);

      // set default preferences
      factory._setPreferences(defaultPreferences);
      // update preferences
      factory.updatePreferences(newPreferences);

      // expecting POST
      httpBackend.expectPOST('/api/preferences');

      // flush command
      httpBackend.flush();

      // now, check preferences
      let preferences = factory.getPreferences();

      expect(preferences.pref1).toEqual(defaultPreferences.pref1);
      expect(preferences.pref2).toEqual(newPreferences.pref2);
    }
  );

  /**
   * Check that we're able to delete preferences
   */
  it('Remove preferences', function () {

      let defaultPreferences = {pref1: 'value1', pref2: 'value2'};
      cheBackend.addDefaultPreferences(defaultPreferences);

      // setup backend
      cheBackend.setup();

      // remove preference
      factory.removePreferences(['pref1']);

      // expecting POST
      httpBackend.expectDELETE('/api/preferences');

      // flush command
      httpBackend.flush();
    }
  );
});
