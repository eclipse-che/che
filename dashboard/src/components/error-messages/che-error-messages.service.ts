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

/**
 * Subscribes callbacks and notifies them if corresponding messages list is changed.
 *
 * @author Oleksii Kurinnyi
 */
export class CheErrorMessagesService {
  messages: {
    [namespace: string]: {
      [messageName: string]: string[];
    }
  } = {};
  callbacks: {
    [namespace: string]: Function[];
  } = {};

  getMessages(namespace: string): string[] {
    if (!this.messages[namespace]) {
      return [];
    }

    let messagesList = [];
    Object.keys(this.messages[namespace]).forEach((messageName: string) => {
      let messages = this.messages[namespace][messageName];
      messages.forEach((message: string) => {
        messagesList.push(messageName + ': ' + message);
      });
    });
    return messagesList;
  }

  addMessage(namespace: string, messageName: string, message: string): void {
    if (!this.messages[namespace]) {
      this.messages[namespace] = {};
    }
    if (!this.messages[namespace][messageName]) {
      this.messages[namespace][messageName] = [];
    }

    if (this.messages[namespace][messageName].indexOf(message) === -1) {
      this.messages[namespace][messageName].push(message);
    }

    this.publishMessages(namespace);
  }

  removeMessages(namespace: string): void {
    if (this.messages[namespace]) {
      this.messages[namespace] = {};
    }

    this.publishMessages(namespace);
  }

  registerCallback(namespace: string, callback: Function): void {
    if (!this.callbacks[namespace]) {
      this.callbacks[namespace] = [];
    }
    if (this.callbacks[namespace].indexOf(callback) === -1) {
      this.callbacks[namespace].push(callback);
    }
  }

  unregisterCallbacks(namespace: string): void {
    this.callbacks[namespace] = [];
  }

  publishMessages(namespace: string): void {
    if (!this.callbacks[namespace] || this.callbacks[namespace].length === 0) {
      return;
    }

    let messages = this.getMessages(namespace);
    this.callbacks[namespace].forEach((callback: Function) => {
      callback(messages);
    });
  }
}
