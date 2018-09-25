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
import {CheWorkspace} from './workspace/che-workspace.factory';
import {ICheRecipeService} from '../../app/workspaces/workspace-details/che-recipe.service';

interface IRemoteStackAPI<T> extends ng.resource.IResourceClass<T> {
  getStacks(): ng.resource.IResource<T>;
  getStack(data: { stackId: string }): ng.resource.IResource<T>;
  updateStack(data: { stackId: string }, stack: che.IStack): ng.resource.IResource<T>;
  createStack(data: Object, stack: che.IStack): ng.resource.IResource<T>;
  deleteStack(data: { stackId: string }): ng.resource.IResource<T>;
}

/**
 * This class is handling the stacks retrieval
 * It sets to the array of stacks
 * @author Florent Benoit
 * @author Ann Shumilova
 */
export class CheStack {

  static $inject = ['$resource', '$q', 'cheWorkspace', 'cheRecipeService'];

  $resource: ng.resource.IResourceService;
  stacksById: { [stackId: string]: che.IStack };
  stacks: Array<any>;
  usedStackNames: Array<string>;
  remoteStackAPI: IRemoteStackAPI<any>;
  /**
   * Angular promise service.
   */
  private $q: ng.IQService;
  /**
   * Workspace API interaction.
   */
  private cheWorkspace: CheWorkspace;
  /**
   * Recipe service.
   */
  private cheRecipeService: ICheRecipeService;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, cheWorkspace: CheWorkspace, cheRecipeService: ICheRecipeService) {

    // keep resource
    this.$resource = $resource;
    this.$q = $q;
    this.cheWorkspace = cheWorkspace;
    this.cheRecipeService = cheRecipeService;

    // stacks per id
    this.stacksById = {};

    // stacks
    this.stacks = [];

    // stack names
    this.usedStackNames = [];

    // remote call
    this.remoteStackAPI = <IRemoteStackAPI<any>>this.$resource('/api/stack', {}, {
      getStacks: {method: 'GET', url: '/api/stack?maxItems=50', isArray: true}, // todo: 50 items is temp solution while paging is not added
      getStack: {method: 'GET', url: '/api/stack/:stackId'},
      updateStack: {method: 'PUT', url: '/api/stack/:stackId'},
      createStack: {method: 'POST', url: '/api/stack'},
      deleteStack: {method: 'DELETE', url: '/api/stack/:stackId'}
    });
  }

  /**
   * Gets stack template
   * @returns {che.IStack}
   */
  getStackTemplate(): che.IStack {
    const supportedTypes = this.cheWorkspace.getSupportedRecipeTypes();

    const type = supportedTypes.find((supportedType: string) => {
      return this.cheRecipeService.isCompose(supportedType) || this.cheRecipeService.isOpenshift(supportedType);
    });
    const machineName = (this.cheRecipeService.isKubernetes(type) || this.cheRecipeService.isOpenshift(type)) ? 'pod/main' : 'main';
    const createDefaultRecipe = (type: string, image: string, machineName: string): che.IRecipe => {
      const nameArray = machineName.split(/\//);
      const opensfiftContent = `kind: List\nitems:\n-\n  apiVersion: v1\n  kind: Pod\n  metadata:\n    name: ${nameArray[0]}\n  spec:\n    containers:\n      -\n        image: ${image}\n        name: ${nameArray[1]}`;
      const recipe: che.IRecipe = {
        content: this.cheRecipeService.isOpenshift(type) ? opensfiftContent : `services:\n ${machineName}:\n  image: ${image}\n`,
        contentType: 'application/x-yaml',
        type: type
      };
      if (this.cheRecipeService.isCompose(type) || this.cheRecipeService.isKubernetes(type) || this.cheRecipeService.isOpenshift(type)) {
        recipe.contentType = 'application/x-yaml';
      }
      return recipe;
    };

    const stack = <che.IStack>{
      'name': 'New Stack',
      'description': 'New Java Stack',
      'scope': 'general',
      'tags': [
        'Java 1.8'
      ],
      'components': [],
      'workspaceConfig': {
        'projects': [],
        'environments': {
          'default': {
            'machines': {
              [machineName]: {
                'installers': [
                  'org.eclipse.che.exec', 'org.eclipse.che.terminal', 'org.eclipse.che.ws-agent', 'org.eclipse.che.ssh'
                ],
                'servers': {},
                'attributes': {
                  'memoryLimitBytes': '2147483648'
                }
              }
            },
            'recipe': createDefaultRecipe(type, 'eclipse/ubuntu_jdk8', machineName)
          }
        },
        'name': 'default',
        'defaultEnv': 'default',
        'commands': []
      }
    };

    let stackName = stack.name;
    let counter = 1;
    do {
      /* tslint:disable */
      stackName = stack.name + '-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4));
      /* tslint:enable */
      counter++;
    } while (!this.isUniqueName(stackName) && counter < 1000);
    stack.name = stackName;

    return stack;
  }

  /**
   * Check if the stack's name is unique.
   * @param name {string}
   * @returns {boolean}
   */
  isUniqueName(name: string): boolean {
    return this.usedStackNames.indexOf(name) === -1;
  }

  /**
   * Fetch the stacks
   * @returns {ng.IPromise<any>}
   */
  fetchStacks(): ng.IPromise<any> {
    let promise = this.remoteStackAPI.getStacks().$promise;
    let updatedPromise = promise.then((stacks: Array<che.IStack>) => {
      // reset global stacks list
      this.stacks.length = 0;
      Object.keys(this.stacksById).forEach((stacksId: string) => {
        delete this.stacksById[stacksId];
      });
      // reset global stack names list
      this.usedStackNames.length = 0;
      stacks.forEach((stack: che.IStack) => {
        this.usedStackNames.push(stack.name);
        // add element on the list
        this.stacksById[stack.id] = stack;
        this.stacks.push(stack);
      });
      return this.$q.when(this.stacks);
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.$q.when(this.stacks);
      }
      return this.$q.reject(error);
    });

    return updatedPromise;
  }

  /**
   * Gets all stacks
   * @returns {Array<che.IStack>}
   */
  getStacks(): Array<che.IStack> {
    return this.stacks;
  }

  /**
   * The stacks per id
   * @param id {string}
   * @returns {che.IStack}
   */
  getStackById(id: string): che.IStack {
    return this.stacksById[id];
  }

  /**
   * Creates new stack.
   * @param stack {che.IStack} - data for new stack
   * @returns {ng.IPromise<any>}
   */
  createStack(stack: che.IStack): ng.IPromise<any> {
    return this.remoteStackAPI.createStack({}, stack).$promise;
  }

  /**
   * Fetch pointed stack.
   * @param stackId {string} - stack's id
   * @returns {ng.IPromise<any>}
   */
  fetchStack(stackId: string): ng.IPromise<any> {
    return this.remoteStackAPI.getStack({stackId: stackId}).$promise;
  }

  /**
   * Update pointed stack.
   * @param stackId {string} - stack's id
   * @param stack {che.IStack} - data for new stack
   * @returns {ng.IPromise<any>}
   */
  updateStack(stackId: string, stack: che.IStack): ng.IPromise<any> {
    return this.remoteStackAPI.updateStack({stackId: stackId}, stack).$promise;
  }

  /**
   * Delete pointed stack.
   * @param stackId {string} - stack's id
   * @returns {ng.IPromise<any>}
   */
  deleteStack(stackId: string): ng.IPromise<any> {
    return this.remoteStackAPI.deleteStack({stackId: stackId}).$promise;
  }
}
