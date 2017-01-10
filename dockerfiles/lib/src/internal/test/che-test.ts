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
// imports


import {Argument} from "../../spi/decorator/parameter";
import {ArgumentProcessor} from "../../spi/decorator/argument-processor";
import {PostFlightCheckTest} from "./impl/post-flight-check-test";
import {Log} from "../../spi/log/log";
import {ProductName} from "../../utils/product-name";
/**
 * Entrypoint for the Tests.
 * @author Florent Benoit
 */
export class CheTest {

    /**
     * This test name will be injected automatically.
     */
    @Argument({description: "Name of the test to execute"})
    testName : string;

    /**
     * Parsing of arguments.
     */
    args : Array<string>;

    /**
     * Map of tests that are available.
     */
    mapOfTests : Map<string, any> = CheTest.init();

    static init() : Map<string,any> {
        Log.context = '(' + ProductName.getMiniDisplayName() + ' test)';
        let testMap : Map<string, any> = new Map<string, any>();
        testMap.set('post-flight-check', PostFlightCheckTest);
        return testMap;
    }

    /**
     * Analyze the arguments by injecting parameters/arguments and define the list of test classes.
     * @param args
     */
    constructor(args:Array<string>) {
        this.args = ArgumentProcessor.inject(this, args);
    }

   /**
     * Run this che-test entry point.
     * When a test is found, build an instance of the test and call run() method which returns a promise
     */
    run() : Promise<any> {
       let classOfTest: any = this.mapOfTests.get(this.testName);
       if (classOfTest) {
           // update logger
           Log.context = '(' + ProductName.getMiniDisplayName() + ' test/' + this.testName + ')';
           var instance = new classOfTest(this.args);
           return instance.run();
       } else {
           // The given test name has not been found, display available actions
           Log.getLogger().error("No test exists with provided name '" + this.testName + "'.");
           this.help();
           process.exit(1);
       }
   }



    help() : void {
        Log.getLogger().info("Available tests are : ");
        for (var [key, value] of this.mapOfTests.entries()) {
            Log.getLogger().info('\u001b[1m' + key + '\u001b[0m');
            ArgumentProcessor.help(Object.create(value.prototype));
        }
    }

}
