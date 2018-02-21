/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

export interface IObservable<T> {
  subscribe(action: IObservableCallbackFn<T>): void;
  publish(arg: T): void;
  unsubscribe(action: IObservableCallbackFn<T>): void;
}

export interface IObservableCallbackFn<T> {
  (arg: T): void;
}

/**
 * Simple implementation of observable pattern.
 *
 * @author Oleksii Kurinnyi
 */
export class Observable<T> implements IObservable<T> {
  /**
   * The list of actions.
   */
  private actions: Array<IObservableCallbackFn<T>>;

  /**
   * Default constructor that is using resource injection
   */
  constructor() {
    this.actions = [];
  }

  /**
   * Subscribes actions.
   *
   * @param {callback} action the action's callback
   */
  subscribe(action: IObservableCallbackFn<T>): void {
    this.actions.push(action);
  }

  /**
   * Unsubscribes the action.
   *
   * @param {callback} action the action's callback.
   */
  unsubscribe(action: IObservableCallbackFn<T>): void {
    this.actions = this.actions.filter((_action: IObservableCallbackFn<T>) => {
      return _action !== action;
    });
  }

  /**
   * Publish data to subscribers.
   *
   * @param {T} arg data
   */
  publish(arg: T): void {
    this.actions.forEach((action: IObservableCallbackFn<T>) => {
      action(arg);
    });
  }

}
