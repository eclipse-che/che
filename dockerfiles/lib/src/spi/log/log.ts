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

import {ProductName} from "../../utils/product-name";
/**
 * Logging class allowing to log message
 * @author Florent Benoit
 */
export class Log {

    static debugEnabled : boolean = false;
    static context : string = ProductName.getDisplayName();
    static logger : Log;
    static disabledPrefix : boolean = false;

    static BLUE: string = '\u001b[34m';
    static GREEN : string = '\u001b[32m';
    static RED : string = '\u001b[31m';
    static ORANGE: string = '\u001b[33m';
    static NC : string = '\u001b[39m';

    static INFO_PREFIX: string = Log.GREEN +  'INFO:' + Log.NC;
    static DEBUG_PREFIX: string = Log.BLUE +  'DEBUG:' + Log.NC;
    static WARN_PREFIX: string = Log.ORANGE +  'WARN:' + Log.NC;
    static ERROR_PREFIX: string = Log.RED +  'ERROR:' + Log.NC;

    static getLogger() : Log {
        if (!Log.logger) {
            Log.logger = new Log();
        }
        return Log.logger;
    }

    info(message : string, ...optional: Array<any>) {
        this.log('info', message, optional);
    }

    warn(message : string, ...optional: Array<any>) {
        this.log('warn', message, optional);
    }

    error(message : string, ...optional: Array<any>) {
        this.log('error', message, optional);
    }

    debug(message : string, ...optional: Array<any>) {
        if (Log.debugEnabled) {
            this.log('debug', message, optional);
        }
    }

    /**
     * Direct option is that it doesn't show any logger trace
     */
    direct(message : string, ...optional: Array<any>) {
        this.log('direct', message, optional);
    }


    log(type: LogType, message: string, optional?: Array<any>) {
        let useContext : boolean = true;
        var prefix: String;
        var displayEachLine : boolean = false;
        if ('info' === type) {
            prefix = Log.INFO_PREFIX;
        } else if ('debug' === type) {
            prefix = Log.DEBUG_PREFIX;
        } else if ('warn' === type) {
            prefix = Log.WARN_PREFIX;
        } else if ('error' === type) {
            prefix = Log.ERROR_PREFIX;
        } else if ('direct' === type) {
            prefix = '';
            useContext = false;
        } else if ('multiline:info' === type) {
            prefix = Log.INFO_PREFIX;
            displayEachLine = true;
        } else if ('multiline:direct' === type) {
            prefix = '';
            useContext = false;
            displayEachLine = true;
        }

        if (useContext && Log.context) {
            prefix += ' ' + Log.context + ': ';
        }
        if (Log.disabledPrefix) {
            prefix = '';
        }

        var consoleMethod : any;
        if ('error' === type) {
            consoleMethod = console.error;
        } else {
            consoleMethod = console.log;
        }

        if (displayEachLine) {
            message.split("\n").forEach((line) => {
                consoleMethod(prefix + line);
            })
        } else {
            if (optional && optional.length > 0) {
                consoleMethod(prefix + message, optional.join(' '));
            } else {
                consoleMethod(prefix + message);
            }
        }

    }


    static enableDebug() : void {
        Log.debugEnabled = true;
    }

    static disablePrefix() : void {
        Log.disabledPrefix = true;
    }

}


export type LogType = 'info' | 'debug' | 'warn' | 'error' | 'direct' | 'multiline:info' | 'multiline:direct';
