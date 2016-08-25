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

import {ArgumentTypeDesc} from "./parameter";
import {ParameterTypeDesc} from "./parameter";
import {Log} from "../log/log";

/**
 * Manage the injection of annotations @Parameter or @Argument
 * @author Florent Benoit
 */
export class ArgumentProcessor {

    static inject(object : any, args: Array<string>) : Array<string> {
        var metadataArguments:Array<ArgumentTypeDesc> = object.__arguments;
        var metadataParameters:Array<ParameterTypeDesc> = object.__parameters;

        var updatedArgs:Array<string> = args;
        if (!updatedArgs) {
            updatedArgs = new Array<string>();
        }

        // handle special help feature
        if (updatedArgs.length == 1 && ('--help' === updatedArgs[0] || 'help' === updatedArgs[0])) {
            // display help menu
            if (metadataParameters) {
                metadataParameters.forEach((metadataParameter) => {
                    Log.getLogger().info('Parameter ' + metadataParameter.names, '\t\t', metadataParameter.description);
                });
            }

            if (metadataArguments) {
                metadataArguments.forEach((metadataArgument) => {
                    Log.getLogger().info(metadataArgument.fieldName, '\t\t', metadataArgument.description);
                });
            }
            process.exit(0);
        }


        var optionsEnabled : Array<string> = new Array<string>();

        // loop on each argument
        if (metadataParameters) {
            var i : number = 0;
            while (i  < updatedArgs.length) {

                var currentArg : string = updatedArgs[i];
                Log.getLogger().debug('checking currentArg', currentArg);


                metadataParameters.forEach((metadataParameter) => {
                    metadataParameter.names.forEach((name) => {

                        Log.getLogger().debug('checking metadataParameter', name + '=', 'against', currentArg);

                        // toggle like '--p=value'
                        if (ArgumentProcessor.startsWith(currentArg, name + '=')) {
                            Log.getLogger().debug('it is equals to name=');
                            // need to split key from value as it's --key=value
                            let splitter:string[] = currentArg.split("=");
                            let value = splitter[1];
                            object[metadataParameter.fieldName] = value;
                            optionsEnabled.push(name + '(' + metadataParameter.description + ') =>' + value);
                            Log.getLogger().debug('= removedOptionsArgs before', updatedArgs, 'with index=', i);
                            updatedArgs.splice(i, 1);
                            i--;
                            Log.getLogger().debug('= match removedOptionsArgs after', updatedArgs, 'with index=', i);
                            Log.getLogger().debug('= match is object[metadataParameter.fieldName] ', object[metadataParameter.fieldName] );
                        } else if (ArgumentProcessor.startsWith(currentArg, name)) {
                            // flag is matching exact argument without value
                            Log.getLogger().debug('exact match is', name);
                            if (metadataParameter.type === 'Boolean') {
                                object[metadataParameter.fieldName] = true;
                            }
                            Log.getLogger().debug('exact match is object[metadataParameter.fieldName] ', object[metadataParameter.fieldName] );
                            optionsEnabled.push(name + '(' + metadataParameter.description + ') =>' + object[metadataParameter.fieldName]);
                            Log.getLogger().debug('exact match removedOptionsArgs before', updatedArgs, 'with index=', i);
                            updatedArgs.splice(i, 1);
                            i--;
                            Log.getLogger().debug('exact match removedOptionsArgs and after we have', updatedArgs);
                        } else if (name === currentArg && ArgumentProcessor.startsWith(currentArg, '-')) {
                            // like -a
                            if (metadataParameter.type === 'Boolean') {
                                object[metadataParameter.fieldName] = true;
                                updatedArgs.splice(i, 1);
                                i--;
                            } else {
                                object[metadataParameter.fieldName] = updatedArgs[i +1];
                                updatedArgs.splice(i, 2);
                                i = i-2;
                            }
                            optionsEnabled.push(name + '(' + metadataParameter.description + ') =>' + object[metadataParameter.fieldName]);

                        }

                    });

                });
                i++;
            }

        }

        // display sum up of what has been processed
        if (optionsEnabled.length > 0) {
            optionsEnabled.forEach((option) => {
                Log.getLogger().debug(option);
            });
        }

        Log.getLogger().debug('start metadata arguments with args', updatedArgs);

        // we have arguments, needs to fill them
        if (metadataArguments) {
            metadataArguments.forEach((argument) => {
                // we're requiring an argument but it's not there
                if (updatedArgs.length == 0) {
                    Log.getLogger().error('Expecting mandatory parameter : ' + argument.description);
                    process.exit(1);
                }

                object[argument.fieldName] = updatedArgs[0];
                // shift args
                updatedArgs = updatedArgs.slice(1);
            })
        }

        Log.getLogger().debug('end metadata arguments with args', updatedArgs);

        return updatedArgs;
    }


    static startsWith(value:string, searchString: string) : boolean {
        return value.substr(0, searchString.length) === searchString;
    }

    static contains(a : Array<any>, obj : any) : boolean{
    for (var i = 0; i < a.length; i++) {
        if (a[i] === obj) {
            return true;
        }
    }
    return false;
}

}