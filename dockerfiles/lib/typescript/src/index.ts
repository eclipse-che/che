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

/// <reference path='./typings/tsd.d.ts' />

import {Argument} from "./spi/decorator/parameter";
import {Parameter} from "./spi/decorator/parameter";
import {ArgumentProcessor} from "./spi/decorator/argument-processor";
import {Log} from "./spi/log/log";
import {CheDir} from "./internal/dir/che-dir";
import {CheTest} from "./internal/test/che-test";
import {CheAction} from "./internal/action/che-action";
/**
 * Entry point of this library providing commands.
 * @author Florent Benoit
 */
export class EntryPoint {

    args: Array<string>;

    @Argument({description: "Name of the command to execute from this entry point."})
    commandName : string;

    @Parameter({names: ["--logger-debug"], description: "Enable the logger in debug mode"})
    debugLogger : boolean;

    @Parameter({names: ["--logger-prefix-off"], description: "Disable prefix mode in logging"})
    prefixOffLogger : boolean;

    constructor() {
        this.args = ArgumentProcessor.inject(this, process.argv.slice(2));

        process.on('SIGINT', () => {
            Log.getLogger().warn('CTRL-C hit, exiting...');
            process.exit(1);
        });

    }

    /**
     * Run this entry point and analyze args to dispatch to the correct entry.
     */
    run() : void {

        // turn into debugging mode
        if (this.debugLogger) {
            Log.enableDebug();
        }

        if (this.prefixOffLogger) {
            Log.disablePrefix();
        }


        var promise : Promise<any>;

        switch(this.commandName) {
            case 'che-test':
                let cheTest: CheTest = new CheTest(this.args);
                promise = cheTest.run();
                break;
            case 'che-action':
                let cheAction: CheAction = new CheAction(this.args);
                promise = cheAction.run();
                break;
            case 'che-dir':
                let cheDir: CheDir = new CheDir(this.args);
                promise = cheDir.run();
                break;
            default:
                Log.getLogger().error('Invalid choice of command-name');
                process.exit(1);
        }

        // handle error of the promise
        promise.catch((error) => {
            try {
                let errorMessage = JSON.parse(error);
                if (errorMessage.message) {
                    Log.getLogger().error(errorMessage.message);
                } else {
                    Log.getLogger().error(error.toString());
                }
            } catch (e) {
                Log.getLogger().error(error.toString());
            }
            process.exit(1);
        });

    }

}


// call run method
new EntryPoint().run();
