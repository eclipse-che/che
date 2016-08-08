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
    startup: Array<String>;

    constructor() {
        this.startup = new Array<String>();
    }
}


export class CheFileStructWorkspaceRuntime {
    recipe: string;
}

export class CheFileStructWorkspaceCommand {
    name: string;
}


export class CheFileStructWorkspace {
    runtime: CheFileStructWorkspaceRuntime;

    name: string;

    commands : Array<CheFileStructWorkspaceCommand>;

    constructor() {
        this.commands = new Array<CheFileStructWorkspaceCommand>();
        this.runtime = new CheFileStructWorkspaceRuntime();
    }
}
