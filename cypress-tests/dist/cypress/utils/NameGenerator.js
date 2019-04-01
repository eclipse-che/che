/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
var NameGenerator = /** @class */ (function () {
    function NameGenerator() {
    }
    NameGenerator.generate = function (prefix, randomLength) {
        var possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        var i;
        var randomPart = "";
        for (i = 0; i < randomLength; i++) {
            var currentRandomIndex = Math.floor(Math.random() * Math.floor(52));
            randomPart += possibleCharacters[currentRandomIndex];
        }
        return prefix + randomPart;
    };
    return NameGenerator;
}());
export { NameGenerator };
