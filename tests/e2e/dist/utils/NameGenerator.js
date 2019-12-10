"use strict";
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
Object.defineProperty(exports, "__esModule", { value: true });
class NameGenerator {
    static generate(prefix, randomLength) {
        const possibleCharacters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
        const possibleCharactersLength = possibleCharacters.length;
        let randomPart = '';
        for (let i = 0; i < randomLength; i++) {
            let currentRandomIndex = Math.floor(Math.random() * Math.floor(possibleCharactersLength));
            randomPart += possibleCharacters[currentRandomIndex];
        }
        return prefix + randomPart;
    }
}
exports.NameGenerator = NameGenerator;
//# sourceMappingURL=NameGenerator.js.map