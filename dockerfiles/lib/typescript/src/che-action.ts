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


import {Argument} from "./parameter/parameter";
import {ArgumentProcessor} from "./parameter/argument-processor";
import {Log} from "./log";
import {CreateStartWorkspaceAction} from "./action/create-start-workspace-action";
import {AddUserAction} from "./action/add-user-action";
import {RemoveUserAction} from "./action/remove-user-action";
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
