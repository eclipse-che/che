/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { CLASSES } from '../inversify.types';
import { inject, injectable } from 'inversify';
import { BrowserTabsUtil } from './BrowserTabsUtil';
import { Logger } from './Logger';

@injectable()
export class WorkspaceNameHandler {

    constructor(@inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil) {}

    public generateWorkspaceName(prefix: string, randomLength: number): string {
        const possibleCharacters: string = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
        const possibleCharactersLength: number = possibleCharacters.length;
        let randomPart: string = '';
        Logger.debug('WorkspaceNameHandler.generateWorkspaceName');

        for (let i = 0; i < randomLength; i++) {
            let currentRandomIndex: number = Math.floor(Math.random() * Math.floor(possibleCharactersLength));

            randomPart += possibleCharacters[currentRandomIndex];
        }

        return prefix + randomPart;
    }

    public async getNameFromUrl(): Promise<string> {
        let workspaceUrl: string = await this.browserTabsUtil.getCurrentUrl();
        Logger.debug(`WorkspaceNameHandler.fromWorkspaceUrl  workspaceUrl: ${workspaceUrl}`);

        const workspaceUrlParts: string[] = workspaceUrl.split('/');
        const workspaceNameQueryString: string = workspaceUrlParts[workspaceUrlParts.length - 1];
        const workspaceName: string = workspaceNameQueryString.split('?')[0];

        Logger.debug(`workspaceName: ${workspaceName}`);

        return workspaceName;
    }
}
