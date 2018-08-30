/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */


export class I18n {

    mapOfConstants : Map<string, string>;

    constructor() {
        this.mapOfConstants = new Map<string, string>();
    }

    get(key: string, ...optional: Array<any>) : string {

        let constant : string = this.mapOfConstants.get(key);
        // not found, return the key
        if (!constant) {
            return key;
        }

        // replace values
        return constant.replace(/{(\d+)}/g, (match, number) => {
            return typeof optional[number] != 'undefined'
                ? optional[number]
                : match
        });
    }


    add(key : string, value : string) {
        this.mapOfConstants.set(key, value);
    }

}