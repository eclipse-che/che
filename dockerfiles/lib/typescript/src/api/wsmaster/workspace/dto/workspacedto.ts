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

export class WorkspaceDto {

    id: string;
    content: any;


    constructor(workspaceObject: any) {
        this.content = workspaceObject;
    }

    getId() : string {
        return this.content.id;
    }

    getName() : string {
        return this.content.config.name;
    }
    getContent() : any {
        return this.content;
    }
}
