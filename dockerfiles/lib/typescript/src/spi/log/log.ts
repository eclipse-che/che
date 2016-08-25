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


import {ProductName} from "../../utils/product-name";
/**
 * Logging class allowing to log message
 * @author Florent Benoit
 */
export class Log {

    static debugEnabled : boolean = false;
    static context : string = ProductName.getShortDisplayName();
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

    log(type: LogType, message: string, optional: Array<any>) {
        var prefix: String;
        if ('info' === type) {
            prefix = Log.INFO_PREFIX;
        } else if ('debug' === type) {
            prefix = Log.DEBUG_PREFIX;
        } else if ('warn' === type) {
            prefix = Log.WARN_PREFIX;
        } else if ('error' === type) {
            prefix = Log.ERROR_PREFIX;
        }

        if (Log.context) {
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


        if (optional) {
            consoleMethod(prefix + message, optional.join(' '));
        } else {
            consoleMethod(prefix + message);
        }

    }


    static enableDebug() : void {
        Log.debugEnabled = true;
    }

    static disablePrefix() : void {
        Log.disabledPrefix = true;
    }

}


export type LogType = 'info' | 'debug' | 'warn' | 'error';
