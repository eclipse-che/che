/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

// tslint:disable:no-any */

export interface IDeferred<T> {
    resolve(value?: T): void;
    reject(reason?: any): void;
    promise: Promise<T>;
}

export class Deferred<T> implements IDeferred<T> {

    promise: Promise<T>;
    private resolveF;
    private rejectF;
    constructor() {
        this.promise = new Promise((resolve, reject) => {
            this.resolveF = resolve;
            this.rejectF = reject;
        });
    }
    resolve(value?: T): void {
        this.resolveF(value);
    }
    reject(reason?: any): void {
        this.rejectF(reason);
    }
}
