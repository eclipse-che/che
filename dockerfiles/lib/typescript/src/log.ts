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


/**
 * Logging class allowing to log message
 * @author Florent Benoit
 */
export class Log {

    static debugEnabled : boolean = false;
    static context : string = 'ECLIPSE CHE';
    static logger : Log;

    BLUE: string = '\u001b[34m';
    GREEN : string = '\u001b[32m';
    RED : string = '\u001b[31m';
    ORANGE: string = '\u001b[33m';
    NC : string = '\u001b[39m';

    INFO_PREFIX: string = this.GREEN +  'INFO:' + this.NC;
    DEBUG_PREFIX: string = this.BLUE +  'DEBUG:' + this.NC;
    WARN_PREFIX: string = this.ORANGE +  'WARN:' + this.NC;
    ERROR_PREFIX: string = this.RED +  'ERROR:' + this.NC;
    
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
            prefix = this.INFO_PREFIX;
        } else if ('debug' === type) {
            prefix = this.DEBUG_PREFIX;
        } else if ('warn' === type) {
            prefix = this.WARN_PREFIX;
        } else if ('error' === type) {
            prefix = this.ERROR_PREFIX;
        }

        if (Log.context) {
            prefix += ' ' + Log.context + ':';
        }

        if (optional) {
            console.log(prefix, message, optional.join(' '));
        } else {
            console.log(prefix, message);
        }
    }

}


export type LogType = 'info' | 'debug' | 'warn' | 'error';
