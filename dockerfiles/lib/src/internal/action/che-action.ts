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
import {Argument} from "../../spi/decorator/parameter";
import {ArgumentProcessor} from "../../spi/decorator/argument-processor";
import {CreateStartWorkspaceAction} from "./impl/create-start-workspace-action";
import {RemoveUserAction} from "./impl/remove-user-action";
import {AddUserAction} from "./impl/add-user-action";
import {ExecuteCommandAction} from "./impl/execute-command-action";
import {Log} from "../../spi/log/log";
import {ListWorkspacesAction} from "./impl/list-workspaces-action";
import {ProductName} from "../../utils/product-name";
import {WorkspaceSshAction} from "./impl/workspace-ssh-action";
import {GetSshDataAction} from "./impl/get-ssh-action";
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
    mapOfActions : Map<string, any> = CheAction.init();

    /**
     * Analyze the arguments by injecting parameters/arguments and define the list of test classes.
     * @param args
     */
    constructor(args:Array<string>) {
        this.args = ArgumentProcessor.inject(this, args);
    }


    static init() : Map<string,any> {
        Log.context = '(' + ProductName.getMiniDisplayName() + ' action)';
        let actionMap : Map<string, any> = new Map<string, any>();
        actionMap.set('create-start-workspace', CreateStartWorkspaceAction);
        actionMap.set('add-user', AddUserAction);
        actionMap.set('remove-user', RemoveUserAction);
        actionMap.set('execute-command', ExecuteCommandAction);
        actionMap.set('list-workspaces', ListWorkspacesAction);
        actionMap.set('workspace-ssh', WorkspaceSshAction);
        actionMap.set('get-ssh-data', GetSshDataAction);

        return actionMap;
    }

   /**
     * Run this che-test entry point.
     * When a test is found, build an instance of the test and call run() method which returns a promise
     */
    run() : Promise<any> {
       let classOfAction: any = this.mapOfActions.get(this.actionName);
       if (classOfAction) {
           Log.context = '(' + ProductName.getMiniDisplayName() + ' action/' + this.actionName + ')';
           var instance = new classOfAction(this.args);
           return instance.run();
       } else {
           // The given action name has not been found, display available actions
           Log.getLogger().error("No action exists with provided name '" + this.actionName + "'.");
           this.help();
           process.exit(1);
       }
   }


    help() : void {
        Log.getLogger().info("Available actions are : ");
        for (var [key, value] of this.mapOfActions.entries()) {
            Log.getLogger().direct('\u001b[1m' + key + '\u001b[0m');
            ArgumentProcessor.help(Object.create(value.prototype));
        }
    }

}
