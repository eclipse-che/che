/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * This class solves problem with losing the `this` binding of instance methods.
 * See http://www.couchcoder.com/angular-1-interceptors-using-typescript/
 */

export abstract class HttpInterceptorBase {
  constructor() {
    ['request', 'requestError', 'response', 'responseError'].forEach((method: string) => {
      if (this[method]) {
        this[method] = this[method].bind(this);
      }
    });
  }
}
