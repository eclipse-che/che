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

import {CheNamespaceRegistry} from '../../../../components/api/namespace/che-namespace-registry.factory';
import {CheUser} from '../../../../components/api/che-user.factory';

/**
 * Service for namespace selector.
 *
 * @author Oleksii Kurinnyi
 */
export class NamespaceSelectorSvc {

  static $inject = ['$location', '$log', '$q', 'cheNamespaceRegistry', 'cheUser'];

  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * The namespace's registry.
   */
  private cheNamespaceRegistry: CheNamespaceRegistry;
  /**
   * User API interaction.
   */
  private cheUser: CheUser;
  /**
   * The list of available namespaces labels.
   */
  private namespaceLabels: string[];

  /**
   * The identifier of current selected namespace.
   */
  private namespaceId: string;
  /**
   * The information of namespaces.
   */
  private namespaceInfo: {
    [namespaceId: string]: string;
  };

  /**
   * Default constructor that is using resource injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $q: ng.IQService,
              cheNamespaceRegistry: CheNamespaceRegistry, cheUser: CheUser) {
    this.$location = $location;
    this.$log = $log;
    this.$q = $q;
    this.cheNamespaceRegistry = cheNamespaceRegistry;
    this.cheUser = cheUser;

    this.namespaceInfo = {};
  }

  /**
   * Fetches namespaces.
   *
   * @return {IPromise<any>}
   */
  fetchNamespaces(): ng.IPromise<any> {
    const namespaces = this.cheNamespaceRegistry.getNamespaces();
    if (namespaces.length) {
      this.namespaceId = namespaces.length ? namespaces[0].id : undefined;
      this.namespaceLabels = namespaces.map((namespace: che.INamespace) => {
        return namespace.label;
      });
      return this.fetchNamespaceInfoById(this.namespaceId);
    }

    return this.cheNamespaceRegistry.fetchNamespaces().then(() => {
      const namespaces = this.getNamespaces();

      if (namespaces.length === 0) {
        return this.$q.when();
      }

      this.namespaceId = namespaces.length ? namespaces[0].id : undefined;
      this.namespaceLabels = namespaces.map((namespace: che.INamespace) => {
        return namespace.label;
      });
      return (this.namespaceId) ? this.fetchNamespaceInfoById(this.namespaceId) : this.$q.when(null);
    }).catch((error: any) => {
      return this.$q.when(null);
    }).then(() => {
      if (this.namespaceId) {
        return this.$q.when();
      }

      const user = this.cheUser.getUser();
      if (user) {
        this.namespaceId = user.name;
        return this.$q.when();
      }

      return this.cheUser.fetchUser().then(() => {
        const user = this.cheUser.getUser();
        this.namespaceId = user.name;
      });
    }).then(() => {
      return this.$q.when(this.namespaceId);
    });
  }

  /**
   * Callback which is called when namespaces changes.
   *
   * @param {string} label a namespace label
   */
  onNamespaceChanged(label: string): void {
    const namespace = this.getNamespaces().find((namespace: any) => {
      return namespace.label === label;
    });
    this.namespaceId = namespace ? namespace.id : this.namespaceId;
    this.fetchNamespaceInfoById(this.namespaceId);
  };

  /**
   * Returns the list of available namespaces.
   *
   * @returns {Array<che.INamespace>} array of namespaces
   */
  getNamespaces(): Array<che.INamespace> {
    return this.cheNamespaceRegistry.getNamespaces() || [];
  }

  getNamespaceLabels(): string[] {
    return this.namespaceLabels;
  }

  /**
   * Returns namespace by its ID
   *
   * @param {string} namespaceId a namespace ID
   * @return {undefined|che.INamespace}
   */
  getNamespaceById(namespaceId: string): che.INamespace {
    return this.getNamespaces().find((namespace: any) => {
      return namespace.id === namespaceId;
    });
  }

  /**
   * Returns namespace by its label.
   *
   * @param {string} label a namespace label
   * @return {undefined|che.INamespace}
   */
  getNamespaceByLabel(label: string): che.INamespace {
    return this.getNamespaces().find((namespace: che.INamespace) => {
      return namespace.label === label;
    });
  }

  /**
   * Fetches namespace info.
   *
   * @param {string} namespaceId a namespace ID
   */
  fetchNamespaceInfoById(namespaceId: string): ng.IPromise<any>  {
    if (!angular.isFunction(this.cheNamespaceRegistry.getAdditionalInfo())) {
      this.namespaceInfo[namespaceId] = null;
      return this.$q.when();
    }

    return this.cheNamespaceRegistry.getAdditionalInfo()(namespaceId).then((info: string) => {
      this.namespaceInfo[namespaceId] = info;
    });
  }

  /**
   * Returns namespace info.
   *
   * @return {string} namespaceId a namespace ID
   */
  getNamespaceInfoById(namespaceId: string): string {
    return this.namespaceInfo[namespaceId];
  }

  /**
   * Returns selected namespace ID.
   *
   * @return {string}
   */
  getNamespaceId(): string {
    return this.namespaceId;
  }

  /**
   * Returns namespaces empty message if set.
   *
   * @returns {string}
   */
  getNamespaceEmptyMessage(): string {
    return this.cheNamespaceRegistry.getEmptyMessage();
  }

  /**
   * Returns namespaces caption.
   *
   * @returns {string}
   */
  getNamespaceCaption(): string {
    return this.cheNamespaceRegistry.getCaption();
  }

}
