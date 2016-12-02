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
export class CheFileStruct {
    server: CheFileServerStruct;

    constructor() {
        this.server = new CheFileServerStruct();
    }
}

export type CheFileServerTypeStruct  = 'local' | 'remote';

export class CheFileServerStruct {

    type : CheFileServerTypeStruct;
    ip: string;
    port: number;
    user: string;
    pass: string;
    startup: Array<string>;

    properties : Map<string, string>;

    constructor() {
        //this.startup = new Array<string>();
        this.properties = new Map<string, string>();
    }
}



export class CheFileStructWorkspaceCommandAttributes {
    previewUrl: string;
}

export interface CheFileStructWorkspaceCommand {
    name: string;
    type: string;
    commandLine: string;
    attributes? : CheFileStructWorkspaceCommandAttributes;
}

export class CheFileStructWorkspaceCommandImpl implements CheFileStructWorkspaceCommand{
    name: string;
    type: string;
    commandLine: string;
    attributes : CheFileStructWorkspaceCommandAttributes;

    constructor() {
        this.type = 'custom';
        this.attributes = new CheFileStructWorkspaceCommandAttributes();
    }
}


export class CheFileStructWorkspaceProjectSourceImpl  {
    attributes : Map<string, string>;
    location: string;
    type: string;

    constructor() {
        this.attributes = new Map<string,string>();
    }
}

export interface CheFileStructWorkspaceProject {
    type: string;
    name : string;
    source : CheFileStructWorkspaceProjectSourceImpl;
}

export class CheFileStructWorkspaceProjectImpl implements CheFileStructWorkspaceProject{
    type: string;
    name : string;
    source : CheFileStructWorkspaceProjectSourceImpl;
    constructor() {
        this.type = 'blank';
        this.source = new CheFileStructWorkspaceProjectSourceImpl();
    }
}


export class CheFileStructWorkspace {

    name: string;

    ram: number;
    commands : Array<CheFileStructWorkspaceCommand>;

    postload : CheFileStructWorkspacePostLoad;

    runtime : CheFileStructWorkspaceRuntime;

    projects: Array<CheFileStructWorkspaceProject>;

    constructor() {
        this.commands = new Array<CheFileStructWorkspaceCommand>();
        this.projects = new Array<CheFileStructWorkspaceProject>();
        this.postload = new CheFileStructWorkspacePostLoad();
        this.runtime = new CheFileStructWorkspaceRuntime();
        // init some commands
        for (let i : number = 0; i < 255; i++) {
            this.commands[i] = new CheFileStructWorkspaceCommandImpl();
        }

        // init some commands
        for (let i : number = 0; i < 255; i++) {
            this.projects[i] = new CheFileStructWorkspaceProjectImpl();
        }

    }
}


export class CheFileStructWorkspaceRuntimeDocker {
    content : string;
    image : string;
    location: string;
}

export class CheFileStructWorkspaceRuntime {
    docker : CheFileStructWorkspaceRuntimeDocker;
    
    constructor() {
        this.docker = new CheFileStructWorkspaceRuntimeDocker();
      }
}

export class CheFileStructWorkspaceLoadingAction {
    command: string;

    script : string;
}

export class CheFileStructWorkspacePostLoad {
    actions : Array<CheFileStructWorkspaceLoadingAction>;

    constructor() {
        this.actions = new Array<CheFileStructWorkspaceLoadingAction>();
        // init some commands
        for (let i : number = 0; i < 255; i++) {
            this.actions[i] = new CheFileStructWorkspaceLoadingAction();
        }

    }
}