/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

export interface INamespace {
  id: string;
  label: string;
  location: string;
}

/**
 * Registry for maintaining system namespaces.
 *
 * @author Ann Shumilova
 */
export class CheNamespaceRegistry {
  private $q: ng.IQService;
  private fetchPromise: ng.IPromise<any>;
  private namespaces : INamespace[];

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService) {
    this.$q = $q;
    this.namespaces = [];
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
    if (!this.fetchPromise) {
      let defer = this.$q.defer();
      defer.resolve();
      return defer.promise;
    }

    return this.fetchPromise;
  }

  /**
   * Adds the list of namespaces.
   *
   * @param {INamespace[]} namespaces namespace to be added
   */
  addNamespaces(namespaces : INamespace[]) : void {
    this.namespaces = this.namespaces.concat(namespaces);
  }

  /**
   * Returns the list of available namespaces.
   *
   * @returns {INamespace[]} namespaces
   */
  getNamespaces() : INamespace[] {
    return this.namespaces;
  }
}
