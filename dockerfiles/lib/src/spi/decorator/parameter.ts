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
import 'reflect-metadata';

export interface ParameterType {

    names: Array<string>;

    description? : string;
}

export interface ParameterTypeDesc extends  ParameterType {
    fieldName: string;
    type : string;
}


export interface ArgumentType {

    description? : string;
}

export interface ArgumentTypeDesc extends ArgumentType {
    fieldName: string;
    type : boolean;
}

/**
 * Handle annotation/decorator for the given parameter
 * @param propertyKey
 * @returns {function(any, string, PropertyDescriptor): undefined}
 * @constructor
 */
export function Parameter(parameterType: ParameterType) : any {
    return  (target: any, propertyKey: string, descriptor: PropertyDescriptor) => {

        var t = Reflect.getMetadata("design:type", target, propertyKey);
        let option : ParameterTypeDesc = {names: parameterType.names, description: parameterType.description, fieldName : propertyKey, type : t.name};

        if (target.__parameters) {
            target.__parameters.push(option);
        } else {
            let options : Array<ParameterTypeDesc> = [option];
            target.__parameters = options;
        }

    };

}



/**
 * Handle annotation/decorator for the given parameter
 * @param propertyKey
 * @returns {function(any, string, PropertyDescriptor): undefined}
 * @constructor
 */
export function Argument(argumentType: ArgumentType) : any {
    return  (target: any, propertyKey: string, descriptor: PropertyDescriptor) => {

        var t = Reflect.getMetadata("design:type", target, propertyKey);
        let option : ArgumentTypeDesc = {description: argumentType.description, fieldName : propertyKey, type : t.name};
        if (target.__arguments) {
            target.__arguments.push(option);
        } else {
            let options : Array<ArgumentTypeDesc> = [option];
            target.__arguments = options;
        }


    };


}
