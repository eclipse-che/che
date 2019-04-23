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
        let possibleCharacters: string = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        let i: number;
        let randomPart: string = "";

        for (i = 0; i < randomLength; i++) {
            let currentRandomIndex: number = Math.floor(Math.random() * Math.floor(52));

            randomPart += possibleCharacters[currentRandomIndex];
        }

        return prefix + randomPart;
    }

}
