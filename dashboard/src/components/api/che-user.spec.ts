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
import {CheUser} from './che-user.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheHttpBackend} from './test/che-http-backend';

/**
 * Test of the Codenvy User API
 */
describe('CheUser', () => {

  /**
   * User Factory for the test
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
   * Che backend
   */
  let cheBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject((cheUser: CheUser, cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend) => {

    factory = cheUser;
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
   * Check that we're able to fetch user data
   */
  it('Fetch user', () => {
      // setup tests objects
      let userId = 'idTestUser';
      let email = 'eclipseCodenvy@eclipse.org';

      let testUser = apiBuilder.getUserBuilder().withId(userId).withEmail(email).build();

      // providing request
      // add test user on Http backend
      cheBackend.setDefaultUser(testUser);

      // setup backend for users
      cheBackend.setup();

      // fetch user
      factory.fetchUser(true);

      // expecting GETs
      httpBackend.expectGET('/api/user');

      // flush command
      httpBackend.flush();

      // now, check user
      let user = factory.getUser();

      // check id and email
      expect(user.id).toEqual(userId);
      expect(user.email).toEqual(email);
    }
  );

  /**
   * Check that we're able to fetch user data by id
   */
  it('Fetch user by id', () => {
      // setup tests objects
      let userId = 'newIdTestUser';
      let email = 'eclipseCodenvy@eclipse.org';

      let testUser = apiBuilder.getUserBuilder().withId(userId).withEmail(email).build();

      // providing request
      // add test user on Http backend
      cheBackend.addUserById(testUser);

      // setup backend
      cheBackend.setup();

      // fetch user
      factory.fetchUserId(userId);

      // expecting GETs
      httpBackend.expectGET('/api/user/' + userId);

      // flush command
      httpBackend.flush();

      // now, check user
      let user = factory.getUserFromId(userId);

      // check id and email
      expect(user.id).toEqual(userId);
      expect(user.email).toEqual(email);
    }
  );

  /**
   * Check that we're able to fetch user data by email
   */
  it('Fetch user by alias', () => {
      // setup tests objects
      let userId = 'testUser';
      let email = 'eclipseCodenvy@eclipse.org';

      let testUser = apiBuilder.getUserBuilder().withId(userId).withEmail(email).build();

      // providing request
      // add test user on Http backend
      cheBackend.addUserEmail(testUser);

      // setup backend
      cheBackend.setup();

      // fetch user
      factory.fetchUserByAlias(email);

      // expecting GETs
      httpBackend.expectGET('/api/user/find?email=' + email);

      // flush command
      httpBackend.flush();

      // now, check user
      let user = factory.getUserByAlias(email);

      // check id and email
      expect(user.id).toEqual(userId);
      expect(user.email).toEqual(email);
    }
  );

  /**
   * Check that we're able to set attributes into profile
   */
  it('Set password', () => {
      // setup
      let testPassword = 'newTestPassword';

      // setup backend
      cheBackend.setup();

      // fetch profile
      factory.setPassword(testPassword);

      // expecting a POST
      httpBackend.expectPOST('/api/user/password', 'password=' + testPassword);

      // flush command
      httpBackend.flush();
    }
  );

  /**
   * Check that we're able to create user
   */
  it('Create user', () => {
      let user = {
        password: 'password12345',
        email: 'eclipseCodenvy@eclipse.org',
        name: 'testName'
      };

      // setup backend
      cheBackend.setup();

      // create user
      factory.createUser(user.name, user.email, user.password);

      // expecting a POST
      httpBackend.expectPOST('/api/user', user);

      // flush command
      httpBackend.flush();
    }
  );


  /**
   * Gets user page object from response
   */
  it('Gets user page object from response', () => {
      let testUser_1 = apiBuilder.getUserBuilder().withId('testUser1Id').withEmail('testUser1@eclipse.org').build();
      let testUser_2 = apiBuilder.getUserBuilder().withId('testUser2Id').withEmail('testUser2@eclipse.org').build();
      let users = [testUser_1, testUser_2];

      let test_link_1 = 'https://aio.codenvy-dev.com/api/admin/user?skipCount=0&maxItems=5';
      let test_rel_1 = 'first';
      let test_link_2 = 'https://aio.codenvy-dev.com/api/admin/user?skipCount=20&maxItems=5';
      let test_rel_2 = 'last';
      let test_link_3 = 'https://aio.codenvy-dev.com/api/admin/user?skipCount=5&maxItems=5';
      let test_rel_3 = 'next';

      let headersLink = '\<' + test_link_1 + '\>' + '; rel="' + test_rel_1 + '",' +
        '\<' + test_link_2 + '\>' + '; rel="' + test_rel_2 + '",' +
        '\<' + test_link_3 + '\>' + '; rel="' + test_rel_3 + '"';

      // setup backend
      cheBackend.setup();

      // gets page
      let pageObject = factory._getPageFromResponse(users, headersLink);

      // flush command
      httpBackend.flush();

      // check page users and links
      expect(pageObject.users).toEqual(users);
      expect(pageObject.links.get(test_rel_1)).toEqual(test_link_1);
      expect(pageObject.links.get(test_rel_2)).toEqual(test_link_2);
      expect(pageObject.links.get(test_rel_3)).toEqual(test_link_3);
    }
  );

  /**
   * Gets maxItems and skipCount from link params
   */
  it('Gets maxItems and skipCount from link params', () => {
      let skipCount = 20;
      let maxItems = 5;
      let test_link = 'https://aio.codenvy-dev.com/api/admin/user?skipCount=' + skipCount + '&maxItems=' + maxItems;

      // setup backend
      cheBackend.setup();

      // gets page
      let pageParams = factory._getPageParamByLink(test_link);

      // flush command
      httpBackend.flush();

      // check page users and links
      expect(parseInt(pageParams.maxItems, 10)).toEqual(maxItems);
      expect(parseInt(pageParams.skipCount, 10)).toEqual(skipCount);
    }
  );

});
