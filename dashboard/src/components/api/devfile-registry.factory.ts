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
import {CheKeycloak} from './che-keycloak.factory';
import {IChangeMemoryUnit} from '../filter/change-memory-unit/change-memory-unit.filter';

export interface IDevfileMetaData {
  displayName: string;
  description?: string;
  globalMemoryLimit: string;
  icon: string;
  links: any;
  tags: Array<string>;
}

enum MemoryUnit { 'B', 'Ki', 'Mi', 'Gi' }

const DEFAULT_JWTPROXY_MEMORY_LIMIT = '128Mi';// default value for che.server.secure_exposer.jwtproxy.memory_limit

/**
 * This class is handling devfile registry api
 * @author Ann Shumilova
 */
export class DevfileRegistry {
  static $inject = ['$http', '$filter', 'cheKeycloak'];

  /**
   * Angular Http service.
   */
  private $http: ng.IHttpService;

  private $filter: ng.IFilterService;

  private devfilesMap: Map<string, che.IWorkspaceDevfile>;

  private isKeycloackPresent: boolean;

  private jwtproxyMemoryLimitNumber: number;

  /**
   * Default constructor that is using resource
   */
  constructor($http: ng.IHttpService, $filter: ng.IFilterService, cheKeycloak: CheKeycloak) {
    this.$http = $http;
    this.$filter = $filter;

    this.devfilesMap = new Map<string, che.IWorkspaceDevfile>();
    this.isKeycloackPresent = cheKeycloak.isPresent();
    this.jwtproxyMemoryLimitNumber = this.getMemoryLimit(DEFAULT_JWTPROXY_MEMORY_LIMIT);
  }

  fetchDevfiles(location: string): ng.IPromise<Array<IDevfileMetaData>> {
    let promise = this.$http({ 'method': 'GET', 'url': location + '/devfiles/index.json' });
    return promise.then((result: any) => {
      return result.data.map((devfileMetaData: IDevfileMetaData) => {
        let globalMemoryLimitNumber = this.getMemoryLimit(devfileMetaData.globalMemoryLimit);
        // TODO remove this after fixing https://github.com/eclipse/che/issues/11424
        if (this.isKeycloackPresent) {
          globalMemoryLimitNumber += this.jwtproxyMemoryLimitNumber;
        }
        devfileMetaData.globalMemoryLimit = this.$filter<IChangeMemoryUnit>('changeMemoryUnit')(globalMemoryLimitNumber, ['B','GB']);
        return devfileMetaData;
      });
    });
  }

  fetchDevfile(location: string, link: string): ng.IPromise<che.IWorkspaceDevfile> {
    let promise = this.$http({ 'method': 'GET', 'url': location + link });
    return promise.then((result: any) => {
      let devfile = this.devfileYamlToJson(result.data)
      this.devfilesMap.set(location + link, devfile);
      return devfile;
    });
  }

  getDevfile(location: string, link: string): che.IWorkspaceDevfile {
    return this.devfilesMap.get(location + link);
  }

  selfLinkToDevfileId(selfLink: string): string {
      const regExpExecArray = /^\/devfiles\/([A-Za-z0-9_\-]+)\/devfile.yaml$/i.exec(selfLink);
      if (regExpExecArray !== null) {
         return regExpExecArray[1];
      }

      return selfLink;
  }

  devfileIdToSelfLink(devfileId: string): string {
      if (/^[A-Za-z0-9_\-]+$/i.test(devfileId)) {
        return `/devfiles/${devfileId}/devfile.yaml`;
      }

      return devfileId;
  }

  private devfileYamlToJson(yaml: string): che.IWorkspaceDevfile {
    try {
      return jsyaml.load(yaml);
    } catch (e) {
    }
  }

   /**
   * Returns memory limit.
   * @param {string} memoryLimit
   * @returns {number}
   */
  getMemoryLimit(memoryLimit: string): number {
    if (!memoryLimit) {
      return -1;
    }
    const regExpExecArray = /^([0-9]+)([a-zA-Z]{1,3})$/.exec(memoryLimit);
    if (regExpExecArray === null) {
      return -1;
    }
    const [, memoryLimitNumber, memoryLimitUnit] = regExpExecArray;
    const power = MemoryUnit[memoryLimitUnit];
    if (!power) {
      return -1;
    }

    return parseInt(memoryLimitNumber, 10) * Math.pow(1024, power);
  }
}
