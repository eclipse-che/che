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

export function sendGet<T>(url: string): Promise<T> {
    return new Promise((resolve, reject) => {
        let request = new XMLHttpRequest();
        request.open("GET", url);
        request.send();
        request.onreadystatechange = function () {
            if (this.readyState !== 4) { return; }
            if (this.status !== 200) {
                reject(this.status ? this.statusText : "Unknown error");
                return;
            }
            resolve(JSON.parse(this.responseText));
        };

    });
}

export function sendPost<T>(url: string): Promise<T> {
    return new Promise((resolve, reject) => {
        let request = new XMLHttpRequest();
        request.open("POST", url);
        request.send();
        request.onreadystatechange = function () {
            if (this.readyState !== 4) { return; }
            if (this.status !== 200) {
                reject(this.status ? this.statusText : "Unknown error");
                return;
            }
            resolve(JSON.parse(this.responseText));
        };

    });
}
