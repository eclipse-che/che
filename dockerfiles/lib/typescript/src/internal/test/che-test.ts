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
    mapOfTests : Map<string, any>;

    /**
     * Analyze the arguments by injecting parameters/arguments and define the list of test classes.
     * @param args
     */
    constructor(args:Array<string>) {
        this.args = ArgumentProcessor.inject(this, args);
        this.mapOfTests = new Map<string, any>();
        this.mapOfTests.set('post-flight-check', PostFlightCheckTest);
    }

   /**
     * Run this che-test entry point.
     * When a test is found, build an instance of the test and call run() method which returns a promise
     */
    run() : Promise<any> {
       let classOfTest: any = this.mapOfTests.get(this.testName);
       if (classOfTest) {
           // update logger
           Log.context = ProductName.getShortDisplayName() + '(test/' + this.testName + ')';

           var instance = Object.create(classOfTest.prototype);
           // here we use an array of array as constructor instance is an array and apply method is also using array to give parameter
           // so it results in having only the first argument of the array without this hack
           let arrayOfArray : Array<any> = new Array<any>();
           arrayOfArray.push(this.args);
           instance.constructor.apply(instance, arrayOfArray);
           return instance.run();
       } else {
           // The given test name has not been found, display available tests
           Log.getLogger().error('No test-name with this value.');
           var iterator = this.mapOfTests.keys();
           var current = iterator.next();
           while (!current.done) {
               Log.getLogger().info('Available test: ' + current.value);
               current = iterator.next();
           }
           process.exit(1);
       }
   }

}
