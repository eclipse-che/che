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

/**
 * Registry for maintaining system namespaces.
 *
 * @author Ann Shumilova
 */
export class CheNamespaceRegistry {

  static $inject = ['$q', '$interval', '$timeout'];

  private $q: ng.IQService;
  private $interval: ng.IIntervalService;
  private $timeout: ng.ITimeoutService;
  private fetchPromise: ng.IPromise<any>;
  private namespaces : che.INamespace[];
  private emptyMessage: string;
  private caption: string;
  private getAdditionalInfoFunction: Function;

  /**
   * Default constructor that is using resource
   */
  constructor($q: ng.IQService, $interval: ng.IIntervalService, $timeout: ng.ITimeoutService) {
    this.$q = $q;
    this.$interval = $interval;
    this.$timeout = $timeout;
    this.namespaces = [];

    this.caption = 'Namespace';
  }

  /**
   * Store promise that resolves after namespaces are added.
   *
   * @param {ng.IPromise<any>} fetchPromise
   */
  setFetchPromise(fetchPromise: ng.IPromise<any>): void {
    this.fetchPromise = fetchPromise;
  }

  /**
   * Returns promise.
   *
   * @return {ng.IPromise<any>}
   */
  fetchNamespaces(): ng.IPromise<any> {
    const defer = this.$q.defer();

    let intervalPromise, timeoutPromise;
    intervalPromise = this.$interval(() => {
      if (this.fetchPromise) {
        defer.resolve();
        this.$timeout.cancel(timeoutPromise);
        this.$interval.cancel(intervalPromise);
      }
    }, 50);
    timeoutPromise = this.$timeout(() => {
      defer.resolve();
      this.$interval.cancel(intervalPromise);
    }, 1000);

    return defer.promise.then(() => {
      if (this.fetchPromise) {
        return this.fetchPromise;
      } else {
        return this.$q.when();
      }
    });
  }

  /**
   * Adds the list of namespaces.
   *
   * @param {che.INamespace[]} namespaces namespace to be added
   */
  addNamespaces(namespaces : che.INamespace[]) : void {
    this.namespaces = this.namespaces.concat(namespaces);
  }

  /**
   * Returns the list of available namespaces.
   *
   * @returns {che.INamespace[]} namespaces
   */
  getNamespaces() : che.INamespace[] {
    return this.namespaces;
  }

  /**
   * Set empty message (message is displayed, when no namespaces).
   *
   * @param message empty message
   */
  setEmptyMessage(message: string): void {
    this.emptyMessage = message;
  }

  /**
   * Returns empty message to display, when no namespaces.
   *
   * @returns {string}
   */
  getEmptyMessage(): string {
    return this.emptyMessage ? this.emptyMessage : null;
  }

  /**
   * Set display caption of the namespaces.
   *
   * @param caption namespaces caption
   */
  setCaption(caption: string): void {
    this.caption = caption;
  }

  /**
   * Returns the caption of the namespaces.
   *
   * @returns {string} namepsaces caption
   */
  getCaption(): string {
    return this.caption;
  }

  /**
   * Sets the function for retrieving available RAM for the namespaces.
   *
   * @param getAdditionalInfo additional information function
   */
  setGetAdditionalInfo(getAdditionalInfo: Function): void {
    this.getAdditionalInfoFunction = getAdditionalInfo;
  }

  /**
   * Returns function, that returns promise.
   *
   * @returns {Function}
   */
  getAdditionalInfo(): Function {
    return this.getAdditionalInfoFunction;
  }
}
