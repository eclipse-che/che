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
import {CheDir} from "./che-dir";
import {Log} from "./log";
import {Argument} from "./parameter/parameter";
import {ArgumentProcessor} from "./parameter/argument-processor";
import {Parameter} from "./parameter/parameter";
import {CheTest} from "./che-test";

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


    constructor() {
        this.args = ArgumentProcessor.inject(this, process.argv.slice(2));
    }

    /**
     * Run this entry point and analyze args to dispatch to the correct entry.
     */
    run() : void {

        // turn into debugging mode
        if (this.debugLogger) {
            Log.enableDebug();
        }

        var promise : Promise<any>;

        switch(this.commandName) {
            case 'che-test':
                // remove post-check arg for now as it's the only test
                let cheTest: CheTest = new CheTest(this.args);
                promise = cheTest.run();
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
