/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { DriverHelper } from './DriverHelper';
import { e2eContainer } from '../inversify.config';
import { CLASSES } from '../inversify.types';
let driverHelper : DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

export class WorkspaceNameHandler {

    public static generateWorkspaceName(prefix: string, randomLength: number): string {
        const possibleCharacters: string = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
        const possibleCharactersLength: number = possibleCharacters.length;
        let randomPart: string = '';

        for (let i = 0; i < randomLength; i++) {
            let currentRandomIndex: number = Math.floor(Math.random() * Math.floor(possibleCharactersLength));

            randomPart += possibleCharacters[currentRandomIndex];
        }

        return prefix + randomPart;
    }

    public static async getNameFromUrl() : Promise<string> {
        let url : string = await driverHelper.getCurrentUrl();
        url = url.split('?')[0];
        let splittedUrl = url.split(`/`);
        let wsname : string = splittedUrl[splittedUrl.length - 1];
        return wsname;
    }

}
