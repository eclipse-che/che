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

import {CheStack} from '../../../../components/api/che-stack.factory';
import {Observable} from '../../../../components/utils/observable';

/**
 * Service for stack selector.
 *
 * @author Oleksii Kurinnyi
 */
export class StackSelectorSvc extends Observable {
  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Stack API interaction.
   */
  private cheStack: CheStack;
  /**
   * Selected stack ID.
   */
  private stackId: string;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($log: ng.ILogService, $q: ng.IQService, cheStack: CheStack) {
    super();

    this.$log = $log;
    this.$q = $q;
    this.cheStack = cheStack;
  }

  /**
   * Fetch list of available stacks.
   *
   * @return {IPromise<Array<che.IStack>>}
   */
  fetchStacks(): ng.IPromise<any> {
    const stacks = this.cheStack.getStacks();
    if (stacks.length) {

      const defer = this.$q.defer();
      defer.resolve(stacks);
      return defer.promise;
    }

    return this.cheStack.fetchStacks().then(() => {
      return this.$q.when();
    }, (error: any) => {
      if (error && error.status !== 304) {
        this.$log.error(error);
      }
      return this.$q.when();
    });
  }

  /**
   * Callback which is called when stack is selected.
   *
   * @param {string} stackId a stack ID
   */
  onStackSelected(stackId: string): void {
    this.stackId = stackId;

    this.publish();
  }

  /**
   * Returns list of stacks.
   *
   * @return {Array<che.IStack>}
   */
  getStacks(): Array<che.IStack> {
    return this.cheStack.getStacks();
  }

  /**
   * Returns selected stack ID.
   *
   * @return {string}
   */
  getStackId(): string {
    return this.stackId;
  }

  /**
   * Returns stack by its ID.
   *
   * @param {string} stackId a stack ID
   * @return {che.IStack}
   */
  getStackById(stackId: string): che.IStack {
    return this.getStacks().find((stack: che.IStack) => {
      return stack.id === stackId;
    });
  }

}
