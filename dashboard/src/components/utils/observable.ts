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

type callback = (...args: any[]) => any;

export interface IObservable {
  subscribe(action: callback): void;
  publish(...args: any[]): void;
  unsubscribe(action: callback): void;
}

/**
 * Simple implementation of observable pattern.
 *
 * @author Oleksii Kurinnyi
 */
export abstract class Observable implements IObservable {
  /**
   * The list of actions.
   */
  private actions: Array<callback>;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor() {
    this.actions = [];
  }

  /**
   * Subscribes actions.
   *
   * @param {callback} action the action's callback
   */
  subscribe(action: callback): void {
    this.actions.push(action);
  }

  /**
   * Unsubscribes the action.
   *
   * @param {callback} action the action's callback.
   */
  unsubscribe(action: callback): void {
    this.actions = this.actions.filter((_action: callback) => {
      return _action !== action;
    });
  }

  /**
   * Publish any data to subscribers.
   *
   * @param {any[]} args data
   */
  publish(...args: any[]): void {
    this.actions.forEach((action: callback) => {
      action(...args);
    });
  }

}
