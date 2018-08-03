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

import {RemotePageLabels} from './remote-page-labels';

interface ITransformResponse {
  objects: Array<any>;
  links?: Map<string, string>;
}

interface IPageData {
  objects?: Array<any>;
  link?: string;
}

interface IPageParam {
  maxItems: number;
  skipCount: number;
}

const LINK = 'link';
const MAX_ITEMS = 'maxItems';
const SKIP_COUNT = 'skipCount';

interface IPageDataResource<T> extends ng.resource.IResourceClass<T> {
  getPageData(): ng.resource.IResource<T>;
}

/**
 * A helper class to simplify getting paging resource. This class create resource for getting objects from server side
 * per page depends on url and request data. Use fetchObjects(maxItems)  for loading the first page objects
 * The maxItems set the max items per page, otherwise - defaults. Use fetchPageObjects(pageKey) for get any of pages.
 * Used one of these keys: 'first', 'prev', 'next', 'last'  or '1', '2', '3' ... .
 * Use  getPageObjects() to get array of current page objects Use objectMap in constructor to store
 * identical objects in one place. In this case page's class will keep only keys,
 * but  getPageObjects() will prepare an array with real objects.
 *
 * @author Oleksii Orel
 */
export class PageObjectResource {

  private $q: ng.IQService;
  private $resource: ng.resource.IResourceService;
  private remoteDataAPI: IPageDataResource<any>;

  private url: string;
  private data: che.IRequestData;
  private objectKey: string;
  private objectMap: Map<string, any>;

  private pagesInfo: che.IPageInfo;
  private pageObjects: Array<any> = [];
  private objectPagesMap: Map<number, IPageData> = new Map();

  constructor(url: string, data: che.IRequestData, $q: ng.IQService, $resource: ng.resource.IResourceService, objectKey?: string, objectMap?: Map<string, any>) {
    this.$q = $q;
    this.url = url;
    this.data = data;
    this.$resource = $resource;
    this.objectKey = objectKey;
    this.objectMap = objectMap;

    // remote call
    this.remoteDataAPI = <IPageDataResource<any>> this.$resource(url, this.data, {
      getPageData: {
        method: 'GET',
        isArray: false,
        responseType: 'json',
        transformResponse: (data: Array<any>, headersGetter: Function) => {
          return this._getPageFromResponse(data, headersGetter(LINK));
        }
      }
    });

    // set default values
    this.pagesInfo = {countPages: 1, currentPageNumber: 1};
  }

  /**
   * Create response object from data and header link.
   * @param data {Array<any>}
   * @param headersLink {string}
   * @returns {ITransformResponse}
   * @private
   */
  _getPageFromResponse(data: Array<any>, headersLink: string): ITransformResponse {
    let newData = [];
    if (angular.isDefined(data) && angular.isArray(data)) {
      data.forEach((object: any) => {
        // add an object
        if (this.objectKey) {
          let val = object[this.objectKey];
          newData.push(val);
          if (this.objectMap && !angular.equals(object, this.objectMap.get(val))) {
            this.objectMap.set(object[this.objectKey], object);
          }
        } else {
          newData.push(object);
        }
      });
    }
    let links: Map<string, string> = new Map();
    if (!headersLink) {
      return {objects: newData};
    }
    let pattern = new RegExp('<([^>]+?)>.+?rel="([^"]+?)"', 'g');
    let result;
    // look for pattern
    while (result = pattern.exec(headersLink)) {
      // add link
      links.set(result[2], result[1]);
    }
    return {
      objects: newData,
      links: links
    };
  }

  /**
   * Gets page param by link.
   * @param pageLink
   * @returns {IPageParam}
   * @private
   */
  _getPageParamByLink(pageLink: string): IPageParam {
    let lastPageParamMap: Map<string, number> = new Map();
    let pattern = new RegExp('([_\\w]+)=([\\w]+)', 'g');
    let result;
    while (result = pattern.exec(pageLink)) {
      lastPageParamMap.set(result[1], parseInt(result[2], 10));
    }
    let skipCount = lastPageParamMap.get(SKIP_COUNT);
    let maxItems = lastPageParamMap.get(MAX_ITEMS);

    return {
      maxItems: maxItems ? maxItems : 0,
      skipCount: skipCount ? skipCount : 0
    };
  }

  /**
   * Update current page data objects.
   * @param data {ITransformResponse}
   * @private
   */
  _updatePageData(data?: ITransformResponse): void {
    const currentPageNumber = this.pagesInfo.currentPageNumber;
    let pageData: IPageData;
    if (this.objectPagesMap.has(currentPageNumber)) {
      pageData = this.objectPagesMap.get(currentPageNumber);
    } else {
      pageData = {objects: []};
    }
    if (angular.isDefined(data)) {
      pageData.objects = data.objects;
      this.objectPagesMap.set(currentPageNumber, pageData);
    }
    // update current page objects
    this.pageObjects.length = 0;
    if (!angular.isArray(pageData.objects)) {
      return;
    }
    pageData.objects.forEach((object: any) => {
      this.pageObjects.push(object);
    });
  }

  /**
   * Update page links by relative direction ('first', 'prev', 'next', 'last').
   * @param data {ITransformResponse}
   * @private
   */
  _updatePageLinks(data: ITransformResponse): void {
    if (!data || !data.links) {
      return;
    }
    // first page link
    this._updatePagesMapLinks(1, data.links.get(RemotePageLabels.FIRST));
    // last page link
    let lastPageLink = data.links.get(RemotePageLabels.LAST);
    let pageParam = this._getPageParamByLink(lastPageLink);
    let countPages = Math.floor(pageParam.skipCount / pageParam.maxItems) + 1;
    this.pagesInfo.countPages = countPages;
    this._updatePagesMapLinks(countPages, lastPageLink);
    // previous page link
    let prevPageNumber = this.pagesInfo.currentPageNumber - 1;
    this._updatePagesMapLinks(prevPageNumber, data.links.get(RemotePageLabels.PREVIOUS));
    // next page link
    let nextPageNumber = this.pagesInfo.currentPageNumber + 1;
    this._updatePagesMapLinks(nextPageNumber, data.links.get(RemotePageLabels.NEXT));
  }

  /**
   * Update map with page's link.
   * @param pageNumber {number}
   * @param pageLink {string}
   * @private
   */
  _updatePagesMapLinks(pageNumber: number, pageLink: string): void {
    if (!pageNumber || !pageLink) {
      return;
    }
    let pageData: IPageData;
    if (this.objectPagesMap.has(pageNumber)) {
      pageData = this.objectPagesMap.get(pageNumber);
      pageData.link = pageLink;
    } else {
      pageData = {link: pageLink};
      this.objectPagesMap.set(pageNumber, pageData);
    }
  }

  /**
   * Ask for loading the first page objects in asynchronous way.
   * @param maxItems - the max number of items to return
   * @returns {*} the promise
   */
  fetchObjects(maxItems?: number): ng.IPromise<any> {
    let skipCount = 0;
    this.pagesInfo.currentPageNumber = 1;
    if (maxItems) {
      this.data.maxItems = maxItems.toString();
    }
    this.data.skipCount = skipCount.toString();
    let promise = this.remoteDataAPI.getPageData().$promise;

    return promise.then((data: ITransformResponse) => {
      this._updatePageLinks(data);
      this._updatePageData(data);
      return this.$q.when(this.getPageObjects());
    }, (error: any) => {
      if (error && error.status === 304) {
        this._updatePageData();
        return this.$q.when(this.getPageObjects());
      }
      return this.$q.reject(error);
    });
  }

  /**
   * Ask for loading any page objects depends on page key ('first', 'prev', 'next', 'last'  or '1', '2', '3' ...).
   * @param pageKey {string} - the key of page
   * @returns {ng.IPromise<Array<any>>} the promise
   */
  fetchPageObjects(pageKey: string): ng.IPromise<Array<any>> {
    let deferred = this.$q.defer();
    let pageNumber;
    switch (pageKey) {
      case RemotePageLabels.FIRST:
        pageNumber = 1;
        break;
      case RemotePageLabels.PREVIOUS:
        pageNumber = this.pagesInfo.currentPageNumber - 1;
        break;
      case RemotePageLabels.NEXT:
        pageNumber = this.pagesInfo.currentPageNumber + 1;
        break;
      case RemotePageLabels.LAST:
        pageNumber = this.pagesInfo.countPages;
        break;
      default:
        pageNumber = parseInt(pageKey, 10);
    }
    if (isNaN(pageNumber) || pageNumber < 1) {
      deferred.reject({data: {message: 'Error. Invalid page key.'}});
      return deferred.promise;
    }
    let pageData = this.objectPagesMap.get(pageNumber);
    if (pageData && pageData.link) {
      this.pagesInfo.currentPageNumber = pageNumber;

      let pageParam = this._getPageParamByLink(pageData.link);
      this.data.maxItems = pageParam.maxItems.toString();
      this.data.skipCount = pageParam.skipCount.toString();

      let promise = this.remoteDataAPI.getPageData().$promise;
      promise.then((data: ITransformResponse) => {
        this._updatePageLinks(data);
        this._updatePageData(data);
        deferred.resolve(this.getPageObjects());
      }, (error: any) => {
        if (error && error.status === 304) {
          let skipCount = (this.pagesInfo.currentPageNumber - 1) * parseInt(this.data.maxItems, 10);
          this.data.skipCount = skipCount.toLocaleString();
          this._updatePageData();
          deferred.resolve(this.getPageObjects());
        }
        deferred.reject(error);
      });
    } else {
      deferred.reject({data: {message: 'Error. No necessary link.'}});
    }

    return deferred.promise;
  }

  /**
   * Gets the pageInfo object.
   * @returns {IPageInfo}
   */
  getPagesInfo(): che.IPageInfo {
    return this.pagesInfo;
  }

  /**
   * Gets the page objects.
   * @returns {Array<any>}
   */
  getPageObjects(): Array<any> {
    if (angular.isUndefined(this.objectKey) || angular.isUndefined(this.objectMap)) {
      return this.pageObjects;
    }
    return this.pageObjects.map((key: string) => {
      return this.objectMap.get(key);
    });
  }

  /**
   * Gets the object key.
   * @returns {string}
   */
  getObjectKey(): string {
    return this.objectKey;
  }

  /**
   * Gets the object key.
   * @returns {Map<string, any>}
   */
  getPgeObjectsMap():  Map<string, any> {
    return this.objectMap;
  }

  /**
   * Gets the request data object.
   * @returns {che.IRequestData}
   */
  getRequestDataObject(): che.IRequestData {
    return this.data;
  }

  /**
   * Gets the request url.
   * @returns {string}
   */
  getRequestUrl(): string {
    return this.url;
  }

  /**
   * Sets page data.
   * @param data {Array<any>}
   * @param headersLink {string}
   */
  setPageData(data: Array<any>, headersLink: string): void {
    const transformResponse = this._getPageFromResponse(data, headersLink);
    this._updatePageLinks(transformResponse);
    this._updatePageData(transformResponse);
  }
}
