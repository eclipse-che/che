/*
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */

import 'reflect-metadata';
import {I18n} from "./i18n";



/**
 * Handle annotation/decorator for the Message
 * @param propertyKey
 * @returns {function(any, string, PropertyDescriptor): undefined}
 */
export function Message(key: string) : any {
    return  (target: any, propertyKey: string, descriptor: PropertyDescriptor) => {

        var t = Reflect.getMetadata("design:type", target, propertyKey);

        // get file
        let fileName = require('path').resolve(__dirname, '../' + key + '.properties');
        var propertiesContent = require('fs').readFileSync(fileName).toString();

        let i18n : I18n = new I18n();


        require('fs').readFileSync(fileName).toString().split('\n').forEach((line) => {
            let split : Array<string> = line.split('=');
            i18n.add(split[0], split[1]);
        });

        target.i18n = i18n;

    };


}
