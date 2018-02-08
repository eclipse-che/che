/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
declare module 'che' {
    export = che;
}

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
