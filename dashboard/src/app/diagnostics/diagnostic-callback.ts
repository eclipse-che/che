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
import {DiagnosticItem} from './diagnostic-item';
import {DiagnosticCallbackState} from './diagnostic-callback-state';
import {DiagnosticPart} from './diagnostic-part';
import {DiagnosticsController} from './diagnostics.controller';

/**
 * Defines a callback of diagnostic. Allow to notify/report states, messages, etc.
 * @author Florent Benoit
 */
export class DiagnosticCallback {

  /**
   * Parent item.
   */
  private diagnosticPart: DiagnosticPart;

  /**
   * Promise service handling.
   */
  private $q: ng.IQService;

  /**
   * Angular Timeout service
   */
  private $timeout: ng.ITimeoutService;

  /**
   * defered object instance from q service
   */
  private defered: ng.IDeferred<any>;

  /**
   * All timeouts that we're starting as part of this callback. They need to be stopped at the end.
   */
  private timeoutPromises: Array<ng.IPromise<any>>;

  /**
   * Map used to send data across different fonctions.
   */
  private sharedMap: Map<string, any>;

  /**
   * Builder when a new sibling callback is required to be added/build.
   */
  private builder: DiagnosticsController;

  /**
   * Name of the callback.
   */
  private name: string;

  /**
   * Content associated to the callback.
   */
  private content: string;

  /**
   * All items attached to this category.
   */
  private items: Array<DiagnosticItem>;


  /**
   * Constructor.
   */
  constructor($q: ng.IQService, $timeout: ng.ITimeoutService, name: string, sharedMap: Map<string, any>, builder: any, diagnosticPart: DiagnosticPart) {
    this.$q = $q;
    this.$timeout = $timeout;
    this.defered = $q.defer();
    this.timeoutPromises = new Array<ng.IPromise<any>>();
    this.name = name;
    this.sharedMap = sharedMap;
    this.builder = builder;
    this.diagnosticPart = diagnosticPart;
    this.items = new Array<DiagnosticItem>();
  }

  /**
   * Add the given value under the given key name.
   * @param {string} key the key for the storage (a string)
   * @param {any} value the value object
   */
  public shared(key: string, value: any): void {
    this.sharedMap.set(key, value);
  }

  /**
   * Allow to retrieved an object based on its key (string)
   * @param {string} key the string name
   * @returns {undefined|any}
   */
  public getShared(key: string): any {
    return this.sharedMap.get(key);
  }

  /**
   * Adds a running state item [like a new service that is now running received by server side event]
   * @param {string} message the message to display
   * @param {string=}hint extra hint
   */
  public stateRunning(message: string, hint?: string): void {
    this.newItem(message, hint, DiagnosticCallbackState.RUNNING);
    this.cleanup();
    this.defered.resolve(message);
  }

  /**
   * Adds a success item [like the result of a test]
   * @param {string} message the message to display
   * @param {string} hint extra hint
   */
  public success(message: string, hint?: string): void {
    this.newItem(message, hint, DiagnosticCallbackState.OK);
    this.cleanup();
    this.defered.resolve(message);
  }

  /**
   * Notify a failure. note: it doesn't stop the execution flow. A success or an error will come after a failure.
   * @param {string} message the message to display
   * @param {string} hint extra hint
   */
  notifyFailure(message: string, hint?: string): void {
    this.newItem(message, hint, DiagnosticCallbackState.FAILURE);
  }

  /**
   * Only notify a hint.
   * @param {string} hint the hint to display
   */
  notifyHint(hint: string): void {
    this.newItem('', hint, DiagnosticCallbackState.HINT);
  }

  /**
   * Adds an error item [like the result of a test]. Note: it will break the current flow and cancel all existing promises.
   * @param {string} message the message to display
   * @param {string} hint extra hint
   */
  public error(message: string, hint?: string): void {
    this.newItem(message, hint, DiagnosticCallbackState.ERROR);
    this.cleanup();
    this.defered.reject(message);
  }

  /**
   * Add content to the callback
   * @param {string} content the content to add
   */
  public addContent(content: string): void {
    if (!this.content) {
      this.content = content;
    } else {
      this.content += '\n' + content;
    }
  }

  /**
   * Promise associated to this callback
   * @returns {ng.IPromise<any>}
   */
  public getPromise(): ng.IPromise<any> {
    return this.defered.promise;
  }

  /**
   * Delay an error after a timeout. Allow to stop a test if there is no answer after some time.
   * @param {string} message
   * @param {number} delay the number of seconds to wait
   */
  delayError(message: string, delay: number) {
    let promise = this.$timeout(() => {
      this.error(message);
    }, delay);
    this.timeoutPromises.push(promise);
  }

  /**
   * Delay a function after a timeout.
   * @param {any} funct the callback function
   * @param {number} delay the number of seconds to wait
   */
  delayFunction(funct: any, delay: number) {
    let promise = this.$timeout(funct, delay);
    this.timeoutPromises.push(promise);
  }


  /**
   * Cleanup all resources (like current promises)
   */
  protected cleanup(): void {
    this.timeoutPromises.forEach((promise: ng.IPromise<any>) => {
      this.$timeout.cancel(promise);
    });
    this.timeoutPromises.length = 0;
  }

  /**
   * Builder of a sibling callback
   * @param {string} text
   * @returns {DiagnosticCallback}
   */
  newCallback(text: string): DiagnosticCallback {
    return this.builder.newItem(text, this.diagnosticPart);
  }

  /**
   * Build a new item instance
   * @param {any} message the message to store in the item
   * @param {string} hint any hint to add to the item
   * @param {DiagnosticCallbackState} state the state of this newly item
   * @returns {DiagnosticItem} the newly created object
   */
  private newItem(message: any, hint: string, state: DiagnosticCallbackState) {
    let diagnosticItem = new DiagnosticItem();
    diagnosticItem.title = this.name;
    diagnosticItem.message = message;
    if (hint) {
      diagnosticItem.hint = hint;
    }
    diagnosticItem.state = state;
    diagnosticItem.content = this.content;
    this.diagnosticPart.addItem(diagnosticItem);
    this.items.push(diagnosticItem);
    return diagnosticItem;
  }

}
