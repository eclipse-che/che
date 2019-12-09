/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export class NameGenerator {
    public static generate(prefix: string, randomLength: number): string {
        const possibleCharacters: string = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
        const possibleCharactersLength: number = possibleCharacters.length;
        let randomPart: string = '';

        for (let i = 0; i < randomLength; i++) {
            let currentRandomIndex: number = Math.floor(Math.random() * Math.floor(possibleCharactersLength));

            randomPart += possibleCharacters[currentRandomIndex];
        }

        return prefix + randomPart;
    }

}
