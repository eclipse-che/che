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
import {PageObjectResource} from './page-object-resource';
import {RemotePageLabels} from './remote-page-labels';

type Object = {
  id: string,
  attributes?: {
    name?: string
  }
};

type PageBackend = {
  urlRegExp: RegExp,
  headerData: { link?: string },
  objects: Array<Object>
};

/**
 * This class creates mocks for page backend.
 *
 * @author Oleksii Orel
 */
export class PageObjectMock {

  private pageObjectResource: PageObjectResource;
  private maxItems: number;
  private countPages: number;
  private countObjects: number;
  private url: string;
  private pageLabels: Array<string>;
  private pageBackendMap: Map<string, PageBackend> = new Map();

  static $inject = ['pageObjectResource', 'maxItems', 'countObjects'];

  /**
   * Default constructor
   */
  constructor(pageObjectResource: PageObjectResource, maxItems: number, countObjects: number) {
    this.countObjects = countObjects;
    this.maxItems = maxItems;
    this.pageObjectResource = pageObjectResource;
    this.url = pageObjectResource.getRequestUrl();
    this.pageLabels = RemotePageLabels.getValues();
    this.countPages = Math.ceil(countObjects / maxItems);

    this.createPageLabelsData();
  }

  /**
   * Returns page number which depends on the page key.
   * @param pageKey {string}
   * @returns {number}
   */
  getPageNumberByLabel(pageKey: string): number {
    let currentPage = this.pageObjectResource.getPagesInfo().currentPageNumber;
    switch (pageKey) {
      case RemotePageLabels.FIRST:
        currentPage = 1;
        break;
      case RemotePageLabels.NEXT:
        currentPage = currentPage < this.countPages ? currentPage + 1 : this.countPages;
        break;
      case RemotePageLabels.PREVIOUS:
        currentPage = currentPage > 1 ? currentPage - 1 : 1;
        break;
      case RemotePageLabels.LAST:
        currentPage = this.countPages;
        break;
      default:
        currentPage = 1;
    }

    return currentPage;
  }

  /**
   * Creates mock data.
   */
  createPageLabelsData(): void {
    const keys = this.getPageLabels();
    this.pageBackendMap.clear();
    let currentPage: number;
    keys.forEach((key: string) => {
      currentPage = this.getPageNumberByLabel(key);
      let testSkipCount = (currentPage - 1) * this.maxItems;

      let currentPageLength = this.countObjects - ((currentPage - 1) * this.maxItems);
      currentPageLength = currentPageLength < this.maxItems ? currentPageLength : this.maxItems;
      currentPageLength = currentPageLength > 0 ? currentPageLength : 0;

      let first_link = `${this.url}?skipCount=${testSkipCount}&maxItems=${this.maxItems}`;
      let next_link = `${this.url}?skipCount=${currentPage * this.maxItems}&maxItems=${this.maxItems}`;
      let last_link = `${this.url}?skipCount=${(this.countPages - 1) * this.maxItems}&maxItems=${this.maxItems}`;

      let headerLink = `\<${first_link}\>; rel="${keys[0]}",\<${last_link}\>; rel="${keys[2]}",\<${next_link}\>; rel="${keys[1]}"`;
      if (currentPage > 1) {
        // prepare 'prev' link
        let prev_link = `${this.url}?skipCount=${(currentPage - 2) * this.maxItems}&maxItems=${this.maxItems}`;
        // add 'prev' link to header
        headerLink += `,\<${prev_link}\>; rel="${keys[3]}"`;
      }

      let objects: Array<Object> = [];
      for (let n = 0; n < currentPageLength; n++) {
        objects.push({id: `testId_${testSkipCount + n}`, attributes: {name: `testName${testSkipCount + n}`}});
      }

      this.pageBackendMap.set(key, {
        urlRegExp: this.getUrlRegExp(),
        headerData: {link: headerLink},
        objects: objects
      });
    });
  }

  /**
   * Returns page backend.
   * @param pageKey {string}
   * @returns {Map<string, PageBackend>}
   */
  getPagePageBackend(pageKey: string): PageBackend {
    return this.pageBackendMap.get(pageKey);
  }

  /**
   * Update pages data in resource object. This is the same which we could receive from the server side.
   * @param pageKeys {Array<string>}
   */
  prepareResourceObject(pageKeys: Array<string>): void {
    pageKeys.forEach((pageKey: string) => {
      this.pageObjectResource.getPagesInfo().currentPageNumber = this.getPageNumberByLabel(pageKey);
      const {headerData, objects} = this.pageBackendMap.get(pageKey);
      this.pageObjectResource.setPageData(objects, headerData.link);
    });
  }

  /**
   * Gets count of objects.
   * @returns {number}
   */
  getCountObjects(): number {
    return this.countObjects;
  }

  /**
   * Returns regular expression for URL.
   * @returns {RegExp}
   */
  getUrlRegExp(): RegExp {
    return new RegExp(this.url + '?.*$');
  }

  /**
   * Gets page max items.
   * @returns {number}
   */
  getMaxItems(): number {
    return this.maxItems;
  }

  /**
   * Gets count of pages.
   * @returns {number}
   */
  getCountPages(): number {
    return this.countPages;
  }

  /**
   * Gets page labels.
   * @returns {number}
   */
  getPageLabels(): Array<string> {
    return this.pageLabels;
  }
}
