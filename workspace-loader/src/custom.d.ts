/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
declare module 'che' {
    export = che;
}

declare function require(string: string): any;

declare namespace che {
    export interface IWorkspace {
        id?: string;
        projects?: any;
        links?: {
            ide?: string
            [rel: string]: string;
        };
        temporary?: boolean;
        status?: string;
        namespace?: string;
        attributes?: IWorkspaceAttributes;
        config: IWorkspaceConfig;
        runtime?: IWorkspaceRuntime;
        isLocked?: boolean;
        usedResources?: string;
    }

    export interface IWorkspaceConfig {
        name?: string;
        defaultEnv?: string;
        environments: {
            [envName: string]: IWorkspaceEnvironment
        };
        projects?: Array<any>;
        commands?: Array<any>;
    }

    export interface IWorkspaceEnvironment {
        machines: {
            [machineName: string]: IEnvironmentMachine
        };
        recipe: IRecipe;
    }

    export interface IRecipe {
        id?: string;
        content?: string;
        location?: string;
        contentType?: string;
        type: string;
    }

    export interface IEnvironmentMachine {
        installers?: string[];
        attributes?: {
            memoryLimitBytes?: string | number;
            [attrName: string]: string | number;
        };
        servers?: {
            [serverRef: string]: IEnvironmentMachineServer
        };
        volumes?: {
            [volumeRef: string]: IEnvironmentMachineVolume
        };
        env?: { [envName: string]: string };
    }

    export interface IEnvironmentMachineServer {
        port: string | number;
        protocol: string;
        path?: string;
        properties?: any;
        attributes?: {
            [attrName: string]: string | number;
        };
    }

    export interface IEnvironmentMachineVolume {
        path: string;
    }

    export interface IWorkspaceAttributes {
        created: number;
        updated?: number;
        stackId?: string;
        errorMessage?: string;
        [propName: string]: string | number;
    }

    export interface IWorkspaceRuntime {
        activeEnv: string;
        links: any[];
        machines: {
            [machineName: string]: IWorkspaceRuntimeMachine
        };
        owner: string;
        warnings: IWorkspaceWarning[];
    }

    export interface IWorkspaceWarning {
        code?: number;
        message: string;
    }

    export interface IWorkspaceRuntimeMachine {
        attributes: { [propName: string]: string };
        servers: { [serverName: string]: IWorkspaceRuntimeMachineServer };
    }

    export interface IWorkspaceRuntimeMachineServer {
        status: string;
        port: string;
        url: string;
        ref: string;
        protocol: string;
        path: string;
        attributes: { [propName: string]: string };
    }
}
