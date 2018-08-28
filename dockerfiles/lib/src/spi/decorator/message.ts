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
import 'reflect-metadata';
import {I18n} from "../i18n/i18n";
import {Log} from "../log/log";



/**
 * Handle annotation/decorator for the Message
 * @param propertyKey
 * @returns {function(any, string, PropertyDescriptor): undefined}
 */
export function Message(key: string) : any {
    return  (target: any, propertyKey: string, descriptor: PropertyDescriptor) => {

        var t = Reflect.getMetadata("design:type", target, propertyKey);

        let path: any = require('path');

        // get file
        let rootFolder : string = path.resolve(path.resolve(__dirname, '..' + path.sep), '..' + path.sep);
        let filename : string = path.resolve(rootFolder, key + '.properties');

        let i18n:I18n = new I18n();
        try {
            // load file content of message properties file, or display warn if file is not found
            var propertiesContent = require('fs').readFileSync(filename).toString();

            require('fs').readFileSync(filename).toString().split('\n').forEach((line) => {
                let split:Array<string> = line.split('=');
                i18n.add(split[0], split[1]);
            });
        } catch (error) {
            Log.getLogger().warn('Unable to locate file', filename);
        }

        target.i18n = i18n;

    };


}

function walk(dir, results) {
    var list = require('fs').readdirSync(dir)
    list.forEach((file) => {
        file = dir + '/' + file
        var stat = require('fs').statSync(file)
        if (stat && stat.isDirectory()) results = results.concat(walk(file, results))
        else results.push(file)
    })
}