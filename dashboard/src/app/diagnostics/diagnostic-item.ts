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
import {DiagnosticCallbackState} from './diagnostic-callback-state';

/**
 * Item is a result of a test or an event to be notified to end user
 * @author Florent Benoit
 */
export class DiagnosticItem {

  /**
   * Title of the item. Describe the global stuff.
   */
  public title: string;

  /**
   * Message used to be displayed after the title of the item.
   */
  public message: string;

  /**
   * State of the current item.
   */
  public state: DiagnosticCallbackState;

  /**
   * Content is extra verbose stuff that could be displayed as part of the logs of the item.
   */
  public content: string;

  /**
   * Hint to report to the end-user. Something that could be helpful regarding the item.
   */
  public hint: string;

  /**
   * Checks the state of the item
   * @returns {boolean} true if state is OK
   */
  public isOk(): boolean {
    return DiagnosticCallbackState.OK === this.state;
  }

  /**
   * Checks the state of the item
   * @returns {boolean} true if state is OK
   */
  public isSuccess(): boolean {
    return DiagnosticCallbackState.OK === this.state;
  }

  /**
   * Checks the state of the item
   * @returns {boolean} true if state is ERROR
   */
  public isError(): boolean {
    return DiagnosticCallbackState.ERROR === this.state;
  }

  /**
   * Checks the state of the item
   * @returns {boolean} true if state is FAILURE
   */
  public isFailure(): boolean {
    return DiagnosticCallbackState.FAILURE === this.state;
  }

  /**
   * Checks the state of the item
   * @returns {boolean} true if state is RUNNING
   */
  public isRunning(): boolean {
    return DiagnosticCallbackState.RUNNING === this.state;
  }

  /**
   * Checks the state of the item
   * @returns {boolean} true if state is HINT
   */
  public isHint(): boolean {
    return DiagnosticCallbackState.HINT === this.state;
  }

  /**
   * Convert state to friendly text.
   * @returns {any}
   */
  public stateToText(): string {
    switch (this.state) {
      case DiagnosticCallbackState.RUNNING :
        return 'STATE_RUNNING';
      case DiagnosticCallbackState.HINT :
        return 'HINT';
      case DiagnosticCallbackState.OK :
        return 'SUCCESS';
      case DiagnosticCallbackState.FAILURE :
        return 'FAILURE';
      case DiagnosticCallbackState.ERROR :
        return 'ERROR';
    }
  }

}
