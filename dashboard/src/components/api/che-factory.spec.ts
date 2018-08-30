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
import {CheHttpBackend} from './test/che-http-backend';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheFactory} from './che-factory.factory';

/**
 * Test of the Che Factory API
 */
describe('CheFactory', () => {


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
  beforeEach(inject((cheFactory: CheFactory, cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend) => {

    factory = cheFactory;
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
   * Check that we're able to fetch factory data
   */
  it('Fetch factories', () => {
      // setup tests objects
      let maxItem = 3;
      let skipCount = 0;
      let testUser = apiBuilder.getUserBuilder().withId('testUserId').build();
      let testFactory1 = apiBuilder.getFactoryBuilder().withId('testId1').withName('testName1').withCreatorEmail('testEmail1').build();
      let testFactory2 = apiBuilder.getFactoryBuilder().withId('testId2').withName('testName2').withCreatorEmail('testEmail2').build();
      let testFactory3 = apiBuilder.getFactoryBuilder().withId('testId3').withName('testName3').withCreatorEmail('testEmail3').build();
      let testFactory4 = apiBuilder.getFactoryBuilder().withId('testId4').withName('testName4').withCreatorEmail('testEmail4').build();

      // providing requests
      // add test objects on Http backend
      cheBackend.setDefaultUser(testUser);
      cheBackend.addUserFactory(testFactory1);
      cheBackend.addUserFactory(testFactory2);
      cheBackend.addUserFactory(testFactory3);
      cheBackend.addUserFactory(testFactory4);
      cheBackend.setPageMaxItem(maxItem);
      cheBackend.setPageSkipCount(skipCount);

      // setup backend for factories
      cheBackend.factoriesBackendSetup();

      // fetch factory
      factory.fetchFactories(maxItem);

      // expecting GETs
      httpBackend.expectGET('/api/user');

      httpBackend.expectGET('/api/factory/find?creator.userId=' + testUser.id + '&maxItems=' + maxItem + '&skipCount=' + skipCount);
      httpBackend.expectGET('/api/factory/' + testFactory1.id);
      httpBackend.expectGET('/api/factory/' + testFactory2.id);
      httpBackend.expectGET('/api/factory/' + testFactory3.id);

      // flush command
      httpBackend.flush();

      // now, check factories
      let pageFactories = factory.getPageFactories();
      let factory1 = factory.getFactoryById(testFactory1.id);

      let factory2 = factory.getFactoryById(testFactory2.id);
      let factory3 = factory.getFactoryById(testFactory3.id);

      // check id, name and email of pge factories
      expect(pageFactories.length).toEqual(maxItem);
      expect(factory1.id).toEqual(testFactory1.id);
      expect(factory1.name).toEqual(testFactory1.name);
      expect(factory1.creator.email).toEqual(testFactory1.creator.email);
      expect(factory2.id).toEqual(testFactory2.id);
      expect(factory2.name).toEqual(testFactory2.name);
      expect(factory2.creator.email).toEqual(testFactory2.creator.email);
      expect(factory3.id).toEqual(testFactory3.id);
      expect(factory3.name).toEqual(testFactory3.name);
      expect(factory3.creator.email).toEqual(testFactory3.creator.email);
    }
  );

  /**
   * Check that we're able to fetch factory data by id
   */
  it('Fetch factor by id', () => {
      // setup tests objects
      let testFactory = apiBuilder.getFactoryBuilder().withId('testId').withName('testName').withCreatorEmail('testEmail').build();

      // providing request
      // add test factory on Http backend
      cheBackend.addUserFactory(testFactory);

      // setup backend
      cheBackend.factoriesBackendSetup();

      // fetch factory
      factory.fetchFactoryById(testFactory.id);

      // expecting GETs
      httpBackend.expectGET('/api/factory/' + testFactory.id);

      // flush command
      httpBackend.flush();

      // now, check factory
      let targetFactory = factory.getFactoryById(testFactory.id);

      // check id, name and email of factory
      expect(targetFactory.id).toEqual(testFactory.id);
      expect(targetFactory.name).toEqual(testFactory.name);
      expect(targetFactory.creator.email).toEqual(testFactory.creator.email);
    }
  );

  /**
   * Check that we're able to delete factor by id
   */
  it('Delete factor by id', () => {
      // setup tests objects
      let testFactory = apiBuilder.getFactoryBuilder().withId('testId').withName('testName').withCreatorEmail('testEmail').build();

      // providing request
      // add test factory on Http backend
      cheBackend.addUserFactory(testFactory);

      // setup backend
      cheBackend.factoriesBackendSetup();

      // delete factory
      factory.deleteFactoryById(testFactory.id);

      // expecting GETs
      httpBackend.expectDELETE('/api/factory/' + testFactory.id);

      // flush command
      httpBackend.flush();
    }
  );

  /**
   * Gets factory page object from response
   */
  it('Gets factory page object from response', () => {
      let testFactory1 = apiBuilder.getFactoryBuilder().withId('testId1').withName('testName1').withCreatorEmail('testEmail1').build();
      let testFactory2 = apiBuilder.getFactoryBuilder().withId('testId2').withName('testName2').withCreatorEmail('testEmail2').build();
      let factories = [testFactory1, testFactory2];

      let test_link_1 = '/api/factory/find?creator.userId=testUserId&skipCount=0&maxItems=5';
      let test_rel_1 = 'first';
      let test_link_2 = '/api/factory/find?creator.userId=testUserId&skipCount=20&maxItems=5';
      let test_rel_2 = 'last';
      let test_link_3 = '/api/factory/find?creator.userId=testUserId&skipCount=5&maxItems=5';
      let test_rel_3 = 'next';

      let headersLink = '\<' + test_link_1 + '\>' + '; rel="' + test_rel_1 + '",' +
        '\<' + test_link_2 + '\>' + '; rel="' + test_rel_2 + '",' +
        '\<' + test_link_3 + '\>' + '; rel="' + test_rel_3 + '"';

      cheBackend.factoriesBackendSetup();

      // gets page
      let pageObject = factory._getPageFromResponse(factories, headersLink);

      httpBackend.flush();

      // check page factories and links
      expect(pageObject.factories).toEqual(factories);
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
      let test_link = '/api/factory/find?creator.userId=testUserId&skipCount=' + skipCount + '&maxItems=' + maxItems;

      cheBackend.factoriesBackendSetup();

      // gets page
      let pageParams = factory._getPageParamByLink(test_link);

      httpBackend.flush();

      // check page factories and links
      expect(parseInt(pageParams.maxItems, 10)).toEqual(maxItems);
      expect(parseInt(pageParams.skipCount, 10)).toEqual(skipCount);
    }
  );

});
