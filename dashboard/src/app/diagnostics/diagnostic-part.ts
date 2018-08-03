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
import {DiagnosticPartState} from './diagnostic-part-state';
import {DiagnosticCallback} from './diagnostic-callback';

/**
 * A part or category is a grouping capability of Diagnostics callbacks. It allows to group callback in some groups.
 * @author Florent Benoit
 */
export class DiagnosticPart {

  /**
   * The title of the part.
   */
  title: string;

  /**
   * An optional subtitle of this part.
   */
  subtitle: string;

  /**
   * The current state of this category
   */
  state: DiagnosticPartState;

  /**
   * Display icon of this part.
   */
  icon: string;

  /**
   * All items attached to this category.
   */
  private items: Array<DiagnosticItem>;

  /**
   * All callbacks attached to this category.
   */
  private callbacks: Array<DiagnosticCallback>;

  /**
   * Current promises of all callbacks
   */
  private callbackPromises: Array<ng.IPromise<any>>;

  /**
   * Number of callbacks (tests) that have finished.
   */
  private nbEndedCallbacks: number;

  /**
   * Number of callbacks (tests) that have been successful.
   */
  private nbSuccessCallbacks: number;

  /**
   * Number of callbacks that had errors
   */
  private nbErrorCallbacks: number;

  /**
   * Callbacks that have ended
   */
  private callbacksEnded: Array<DiagnosticCallback>;

  /**
   * Constructor.
   */
  constructor() {
    this.items = new Array<DiagnosticItem>();
    this.callbacks = new Array<DiagnosticCallback>();
    this.callbacksEnded = new Array<DiagnosticCallback>();
    this.callbackPromises = new Array<ng.IPromise<any>>();
    this.nbEndedCallbacks = 0;
    this.nbSuccessCallbacks = 0;
    this.nbErrorCallbacks = 0;
  }

  /**
   * Add an item to this part.
   * @param {DiagnosticItem} item the item to add
   */
  addItem(item: DiagnosticItem): void {
    this.items.push(item);
  }

  /**
   * Test callback to add to this part
   * @param {DiagnosticCallback} callback the test callback to add.
   */
  addCallback(callback: DiagnosticCallback): void {

    callback.getPromise().then(() => {
      this.nbSuccessCallbacks++;
    });

    callback.getPromise().catch(() => {
      this.nbErrorCallbacks++;
    });

    callback.getPromise().finally(() => {
      this.callbacksEnded.push(callback);
      this.nbEndedCallbacks++;
    });

    this.callbackPromises.push(callback.getPromise());
    this.callbacks.push(callback);
  }

  /**
   * Checks the state of the part
   * @returns {boolean} true if state is READY
   */
  public isReady(): boolean {
    return DiagnosticPartState.READY === this.state;
  }

  /**
   * Checks the state of the part
   * @returns {boolean} true if state is IN_PROGRESS
   */
  public isInProgress(): boolean {
    return DiagnosticPartState.IN_PROGRESS === this.state;
  }

  /**
   * Checks the state of the part
   * @returns {boolean} true if state is SUCCESS
   */
  public isSuccess(): boolean {
    return DiagnosticPartState.SUCCESS === this.state;
  }

  /**
   * Checks the state of the part
   * @returns {boolean} true if state is FAILURE
   */
  public isFailure(): boolean {
    return DiagnosticPartState.FAILURE === this.state;
  }

  /**
   * Checks the state of the part
   * @returns {boolean} true if state is ERROR
   */
  public isError(): boolean {
    return DiagnosticPartState.ERROR === this.state;
  }

  /**
   * Convert state to friendly text.
   * @returns {string}
   */
  public stateToText(): string {
    switch (this.state) {
      case DiagnosticPartState.READY :
        return 'READY (planned)';
      case DiagnosticPartState.IN_PROGRESS :
        return 'IN PROGRESS';
      case DiagnosticPartState.SUCCESS :
        return 'SUCCESS';
      case DiagnosticPartState.FAILURE :
        return 'FAILURE';
      case DiagnosticPartState.ERROR :
        return 'ERROR';
    }
  }

  /**
   * Clear the values
   */
  clear(): void {
    this.nbEndedCallbacks = 0;
    this.nbSuccessCallbacks = 0;
    this.nbErrorCallbacks = 0;
    this.items.length = 0;
    this.callbacks.length = 0;
    this.callbacksEnded.length = 0;
    this.callbackPromises.length = 0;
  }

  /**
   * Gets the total number of callbacks
   * @returns {number}
   */
  public getNumberOfCallbacks(): number {
    return this.callbacks.length;
  }

  /**
   * Gets the total number of ended callbacks
   * @returns {number}
   */
  public getNumberOfEndedCallbacks(): number {
    return this.nbEndedCallbacks;
  }


}
