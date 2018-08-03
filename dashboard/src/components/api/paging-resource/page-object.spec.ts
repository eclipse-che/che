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
import {CheHttpBackend} from '../test/che-http-backend';
import {CheAPIBuilder} from '../builder/che-api-builder.factory';
import {ChePageObject} from './page-object.factory';
import {PageObjectMock} from './page-object.mock';
import {PageObjectResource} from './page-object-resource';
import {RemotePageLabels} from './remote-page-labels';

describe('PageObject >', () => {
  /**
   * Page object factory for the test
   */
  let factory: ChePageObject;
  /**
   * API builder.
   */
  let apiBuilder: CheAPIBuilder;
  /**
   * Backend for handling http operations
   */
  let $httpBackend: ng.IHttpBackendService;
  /**
   * Che backend
   */
  let cheBackend: CheHttpBackend;

  let pageObjectResource: PageObjectResource;

  let pageObjectMock: PageObjectMock;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject((chePageObject: ChePageObject, cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend) => {
    factory = chePageObject;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    $httpBackend = cheHttpBackend.getHttpBackend();

    cheBackend.setup();

    const maxItems = 15;
    const countObjects = 50;
    const url = '/api/object';

    pageObjectResource = factory.createPageObjectResource(url);
    pageObjectMock = new PageObjectMock(pageObjectResource, maxItems, countObjects);
  }));

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  it('fetch page objects without header link data', () => {
    // prepare mocks
    const {urlRegExp, objects} = pageObjectMock.getPagePageBackend(RemotePageLabels.FIRST);
    const maxItems = pageObjectMock.getMaxItems();
    const callbacks = { testResolve: angular.noop, testReject: angular.noop };
    // create spies
    spyOn(callbacks, 'testResolve');
    spyOn(callbacks, 'testReject');

    $httpBackend.expectGET(urlRegExp).respond(200, objects);

    // fetch first page
    pageObjectResource.fetchObjects(maxItems).then(callbacks.testResolve).catch(callbacks.testReject);

    $httpBackend.flush();

    // page params
    const testCountPages = pageObjectResource.getPagesInfo().countPages;
    const testCurrentPageNumber = pageObjectResource.getPagesInfo().currentPageNumber;
    const testMaxItems = parseInt(pageObjectResource.getRequestDataObject().maxItems, 10);
    const testSkipCount = parseInt(pageObjectResource.getRequestDataObject().skipCount, 10);
    const testObjects = pageObjectResource.getPageObjects();

    // check objects
    expect(callbacks.testResolve).toHaveBeenCalled();
    expect(callbacks.testReject).not.toHaveBeenCalled();
    expect(testObjects).toEqual(objects);
    expect(testMaxItems).toEqual(maxItems);
    expect(testSkipCount).toEqual(0);

    // this is because we haven't header data for paging
    expect(testCurrentPageNumber).toEqual(1);
    expect(testCountPages).toEqual(1);
  });

  it('fetch first page objects with header link data', () => {
    // prepare mocks
    const {urlRegExp, headerData, objects} = pageObjectMock.getPagePageBackend(RemotePageLabels.FIRST);
    const maxItems = pageObjectMock.getMaxItems();
    const countPages = pageObjectMock.getCountPages();
    const callbacks = { testResolve: angular.noop, testReject: angular.noop };
    // create spies
    spyOn(callbacks, 'testResolve');
    spyOn(callbacks, 'testReject');

    $httpBackend.expectGET(urlRegExp).respond(200, objects, headerData);

    // fetch first page
    pageObjectResource.fetchObjects(maxItems).then(callbacks.testResolve).catch(callbacks.testReject);

    $httpBackend.flush();

    // page params
    const testCountPages = pageObjectResource.getPagesInfo().countPages;
    const testCurrentPageNumber = pageObjectResource.getPagesInfo().currentPageNumber;
    const testMaxItems = parseInt(pageObjectResource.getRequestDataObject().maxItems, 10);
    const testSkipCount = parseInt(pageObjectResource.getRequestDataObject().skipCount, 10);
    const testObjects = pageObjectResource.getPageObjects();

    // check objects
    expect(callbacks.testResolve).toHaveBeenCalled();
    expect(callbacks.testReject).not.toHaveBeenCalled();
    expect(testObjects).toEqual(objects);
    expect(testMaxItems).toEqual(maxItems);
    expect(testCountPages).toEqual(countPages);
    expect(testCurrentPageNumber).toEqual(1);
    expect(testSkipCount).toEqual(0);
  });

  it('fetch first page objects by page label.', () => {
    // prepare mocks
    const {urlRegExp, headerData, objects} = pageObjectMock.getPagePageBackend(RemotePageLabels.FIRST);
    const maxItems = pageObjectMock.getMaxItems();
    const countPages = pageObjectMock.getCountPages();
    const callbacks = { testResolve: angular.noop, testReject: angular.noop };
    // create spies
    spyOn(callbacks, 'testResolve');
    spyOn(callbacks, 'testReject');
    // prepare resource object - save first page data into
    pageObjectMock.prepareResourceObject([RemotePageLabels.FIRST]);

    $httpBackend.expectGET(urlRegExp).respond(200, objects, headerData);

    // fetch first page
    pageObjectResource.fetchPageObjects(RemotePageLabels.FIRST).then(callbacks.testResolve).catch(callbacks.testReject);

    $httpBackend.flush();

    // page params
    const testCountPages = pageObjectResource.getPagesInfo().countPages;
    const testCurrentPageNumber = pageObjectResource.getPagesInfo().currentPageNumber;
    const testMaxItems = parseInt(pageObjectResource.getRequestDataObject().maxItems, 10);
    const testSkipCount = parseInt(pageObjectResource.getRequestDataObject().skipCount, 10);
    const testObjects = pageObjectResource.getPageObjects();

    // check objects
    expect(callbacks.testResolve).toHaveBeenCalled();
    expect(callbacks.testReject).not.toHaveBeenCalled();
    expect(testObjects).toEqual(objects);
    expect(testMaxItems).toEqual(maxItems);
    expect(testCountPages).toEqual(countPages);
    expect(testCurrentPageNumber).toEqual(1);
    expect(testSkipCount).toEqual(0);
  });

  it('fetch next page objects by page label.', () => {
    // prepare mocks
    const {urlRegExp, headerData, objects} = pageObjectMock.getPagePageBackend(RemotePageLabels.NEXT);
    const maxItems = pageObjectMock.getMaxItems();
    const countPages = pageObjectMock.getCountPages();
    const callbacks = { testResolve: angular.noop, testReject: angular.noop };
    // create spies
    spyOn(callbacks, 'testResolve');
    spyOn(callbacks, 'testReject');
    // prepare resource object - save first page data into
    pageObjectMock.prepareResourceObject([RemotePageLabels.FIRST]);

    $httpBackend.expectGET(urlRegExp).respond(200, objects, headerData);

    // fetch next page
    pageObjectResource.fetchPageObjects(RemotePageLabels.NEXT).then(callbacks.testResolve).catch(callbacks.testReject);

    $httpBackend.flush();

    // page params
    const testCountPages = pageObjectResource.getPagesInfo().countPages;
    const testCurrentPageNumber = pageObjectResource.getPagesInfo().currentPageNumber;
    const testMaxItems = parseInt(pageObjectResource.getRequestDataObject().maxItems, 10);
    const testSkipCount = parseInt(pageObjectResource.getRequestDataObject().skipCount, 10);
    const testObjects = pageObjectResource.getPageObjects();

    // check objects
    expect(callbacks.testResolve).toHaveBeenCalled();
    expect(callbacks.testReject).not.toHaveBeenCalled();
    expect(testObjects).toEqual(objects);
    expect(testMaxItems).toEqual(maxItems);
    expect(testCountPages).toEqual(countPages);
    expect(testCurrentPageNumber).toEqual(2);
    expect(testSkipCount).toEqual(maxItems);
  });

  it('fetch last page objects by page label.', () => {
    // prepare mocks
    const {urlRegExp, headerData, objects} = pageObjectMock.getPagePageBackend(RemotePageLabels.LAST);
    const maxItems = pageObjectMock.getMaxItems();
    const countPages = pageObjectMock.getCountPages();
    const callbacks = { testResolve: angular.noop, testReject: angular.noop };
    // create spies
    spyOn(callbacks, 'testResolve');
    spyOn(callbacks, 'testReject');
    // prepare resource object - save first, next pages data into
    pageObjectMock.prepareResourceObject([RemotePageLabels.FIRST, RemotePageLabels.NEXT]);
    const currentPageNumber = pageObjectMock.getPageNumberByLabel(RemotePageLabels.LAST);
    const skipCount = (currentPageNumber - 1) * maxItems;

    $httpBackend.expectGET(urlRegExp).respond(200, objects, headerData);

    // fetch next page
    pageObjectResource.fetchPageObjects(RemotePageLabels.LAST).then(callbacks.testResolve).catch(callbacks.testReject);

    $httpBackend.flush();

    // page params
    const testCountPages = pageObjectResource.getPagesInfo().countPages;
    const testCurrentPageNumber = pageObjectResource.getPagesInfo().currentPageNumber;
    const testMaxItems = parseInt(pageObjectResource.getRequestDataObject().maxItems, 10);
    const testSkipCount = parseInt(pageObjectResource.getRequestDataObject().skipCount, 10);
    const testObjects = pageObjectResource.getPageObjects();

    // check objects
    expect(callbacks.testResolve).toHaveBeenCalled();
    expect(callbacks.testReject).not.toHaveBeenCalled();
    expect(testObjects).toEqual(objects);
    expect(testMaxItems).toEqual(maxItems);
    expect(testCountPages).toEqual(countPages);
    expect(testCurrentPageNumber).toEqual(currentPageNumber);
    expect(testSkipCount).toEqual(skipCount);
  });

  it('fetch previous page objects by page label.', () => {
    // prepare mocks
    const {urlRegExp, headerData, objects} = pageObjectMock.getPagePageBackend(RemotePageLabels.PREVIOUS);
    const maxItems = pageObjectMock.getMaxItems();
    const countPages = pageObjectMock.getCountPages();
    const callbacks = { testResolve: angular.noop, testReject: angular.noop };
    // create spies
    spyOn(callbacks, 'testResolve');
    spyOn(callbacks, 'testReject');
    // prepare resource object - save first, next and last pages data into
    pageObjectMock.prepareResourceObject([RemotePageLabels.FIRST, RemotePageLabels.NEXT, RemotePageLabels.LAST]);
    const currentPageNumber = pageObjectMock.getPageNumberByLabel(RemotePageLabels.PREVIOUS);
    const skipCount = (currentPageNumber - 1) * maxItems;

    $httpBackend.expectGET(urlRegExp).respond(200, objects, headerData);

    // fetch next page
    pageObjectResource.fetchPageObjects(RemotePageLabels.PREVIOUS).then(callbacks.testResolve).catch(callbacks.testReject);

    $httpBackend.flush();

    // page params
    const testCountPages = pageObjectResource.getPagesInfo().countPages;
    const testCurrentPageNumber = pageObjectResource.getPagesInfo().currentPageNumber;
    const testMaxItems = parseInt(pageObjectResource.getRequestDataObject().maxItems, 10);
    const testSkipCount = parseInt(pageObjectResource.getRequestDataObject().skipCount, 10);
    const testObjects = pageObjectResource.getPageObjects();

    // check objects
    expect(callbacks.testResolve).toHaveBeenCalled();
    expect(callbacks.testReject).not.toHaveBeenCalled();
    expect(testObjects).toEqual(objects);
    expect(testMaxItems).toEqual(maxItems);
    expect(testCountPages).toEqual(countPages);
    expect(testCurrentPageNumber).toEqual(currentPageNumber);
    expect(testSkipCount).toEqual(skipCount);
  });

  it('should resolve promise with old page data if object\'s request status code is equals 304', () => {
    // prepare mocks
    const {urlRegExp, objects} = pageObjectMock.getPagePageBackend(RemotePageLabels.FIRST);
    const maxItems = pageObjectMock.getMaxItems();
    const countPages = pageObjectMock.getCountPages();
    const callbacks = { testResolve: angular.noop, testReject: angular.noop };
    // create spies
    spyOn(callbacks, 'testResolve');
    spyOn(callbacks, 'testReject');
    // prepare resource object - save first and last pages data into
    pageObjectMock.prepareResourceObject([RemotePageLabels.FIRST, RemotePageLabels.LAST]);

    $httpBackend.expectGET(urlRegExp).respond(304, {});

    // fetch first page again
    pageObjectResource.fetchPageObjects(RemotePageLabels.FIRST).then(callbacks.testResolve).catch(callbacks.testReject);

    $httpBackend.flush();

    // page params
    const testCountPages = pageObjectResource.getPagesInfo().countPages;
    const testCurrentPageNumber = pageObjectResource.getPagesInfo().currentPageNumber;
    const testMaxItems = parseInt(pageObjectResource.getRequestDataObject().maxItems, 10);
    const testSkipCount = parseInt(pageObjectResource.getRequestDataObject().skipCount, 10);
    const testObjects = pageObjectResource.getPageObjects();

    // check objects
    expect(callbacks.testResolve).toHaveBeenCalled();
    expect(callbacks.testReject).not.toHaveBeenCalled();
    expect(testMaxItems).toEqual(maxItems);
    expect(testObjects).toEqual(objects);
    expect(testCountPages).toEqual(countPages);
    expect(testCurrentPageNumber).toEqual(1);
    expect(testSkipCount).toEqual(0);
  });

  it('should reject promise if object\'s request failed', () => {
    // prepare mocks
    const urlRegExp = pageObjectMock.getUrlRegExp();
    const callbacks = { testResolve: angular.noop, testReject: angular.noop };
    // create spies
    spyOn(callbacks, 'testResolve');
    spyOn(callbacks, 'testReject');

    $httpBackend.expectGET(urlRegExp).respond(404);

    // fetch first page
    pageObjectResource.fetchObjects().then(callbacks.testResolve).catch(callbacks.testReject);

    $httpBackend.flush();

    expect(callbacks.testResolve).not.toHaveBeenCalled();
    expect(callbacks.testReject).toHaveBeenCalled();
  });

});
