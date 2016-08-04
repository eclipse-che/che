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
import {PostCheck} from './post-check';
import {CheFile} from "./che-file";
import {Log} from "./log";

/**
 * Entry point of this library providing commands.
 * @author Florent Benoit
 */
export class EntryPoint {

    args: Array<string>;

    constructor() {
        this.args = process.argv.slice(2);
    }

    /**
     * Run this entry point and analyze args to dispatch to the correct entry.
     */
    run() : void {
        var promise : Promise<string>;
        // get first arg if any
        if (this.args.length > 0) {
            let commandName = this.args[0];
            if ('che-test' === commandName) {
                // remove post-check arg for now as it's the only test
                let postCheck: PostCheck = new PostCheck(this.args.slice(2));
                promise = postCheck.run();
            } else if ('che-file' === commandName) {
                let cheFile: CheFile = new CheFile(this.args.slice(1));
                promise = cheFile.run();
            } else {
                this.printHelp();
            }

        }

        // handle error of the promise
        if (promise) {
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

    /**
     * Display the help.
     */
    printHelp() : void {
        Log.getLogger().info('Help: ');
        Log.getLogger().info('      valid options are : [che-test|che-file]');

    }

}


// call run method
new EntryPoint().run();
