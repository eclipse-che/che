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

import {Argument} from "../../spi/decorator/parameter";
import {ArgumentProcessor} from "../../spi/decorator/argument-processor";
import {CreateStartWorkspaceAction} from "./impl/create-start-workspace-action";
import {RemoveUserAction} from "./impl/remove-user-action";
import {AddUserAction} from "./impl/add-user-action";
import {ExecuteCommandAction} from "./impl/execute-command-action";
import {Log} from "../../spi/log/log";
/**
 * Entrypoint for the Actions.
 * @author Florent Benoit
 */
export class CheAction {

    /**
     * This action name will be injected automatically.
     */
    @Argument({description: "Name of the action to execute"})
    actionName : string;

    /**
     * Parsing of arguments.
     */
    args : Array<string>;

    /**
     * Map of tests that are available.
     */
    mapOfActions : Map<string, any>;

    /**
     * Analyze the arguments by injecting parameters/arguments and define the list of test classes.
     * @param args
     */
    constructor(args:Array<string>) {
        this.args = ArgumentProcessor.inject(this, args);
        this.mapOfActions = new Map<string, any>();
        this.mapOfActions.set('create-start-workspace', CreateStartWorkspaceAction);
        this.mapOfActions.set('add-user', AddUserAction);
        this.mapOfActions.set('remove-user', RemoveUserAction);
        this.mapOfActions.set('execute-command', ExecuteCommandAction);


    }

   /**
     * Run this che-test entry point.
     * When a test is found, build an instance of the test and call run() method which returns a promise
     */
    run() : Promise<any> {
       let classOfAction: any = this.mapOfActions.get(this.actionName);
       if (classOfAction) {
           var instance = Object.create(classOfAction.prototype);
           // here we use an array of array as constructor instance is an array and apply method is also using array to give parameter
           // so it results in having only the first argument of the array without this hack
           let arrayOfArray : Array<any> = new Array<any>();
           arrayOfArray.push(this.args);
           instance.constructor.apply(instance, arrayOfArray);
           return instance.run();
       } else {
           // The given test name has not been found, display available actions
           Log.getLogger().error('No action-name with this value.');
           var iterator = this.mapOfActions.keys();
           var current = iterator.next();
           while (!current.done) {
               Log.getLogger().info('Available action: ' + current.value);
               current = iterator.next();
           }
           process.exit(1);
       }
   }

}
