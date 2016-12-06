/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
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