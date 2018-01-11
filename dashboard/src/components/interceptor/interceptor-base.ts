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
