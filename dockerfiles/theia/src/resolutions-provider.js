/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
"use strict";
/*
 * Copyright (c) 2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
const fs = require("fs");
const spawnSync = require("child_process").spawnSync;

const PACKAGE_JSON_PATH = process.argv[2];
console.log(`Generate resolutions for ${PACKAGE_JSON_PATH}`);

const PACKAGE_JSON = require(PACKAGE_JSON_PATH);
const THEIA_VERSION = process.env.THEIA_VERSION;
const HOME = process.env.HOME;

const NPM_API_URL = 'https://api.npms.io/v2';
const keyWords = 'keywords:theia-extension';
const resultSize = 200;

const SEARCH_JSON_PATH = `${HOME}/search.json`;

try {
    spawnSync('curl',[`${NPM_API_URL}/search?q=${keyWords}&size=${resultSize}`, '-o', SEARCH_JSON_PATH]);
} catch(error) {
    console.error("Failed to get Theia depedencies. Cause: ", error);
    process.exit(1);
}

const packageScopeRegexp = '^@theia/.*$';
let theiaResolutionsList = [];
try {
    const filteredDepList = spawnSync('jq', ['-c', `.results | map(.package) | map(.name|select(test("${packageScopeRegexp}")))`, SEARCH_JSON_PATH]);
    theiaResolutionsList = JSON.parse(filteredDepList.stdout);
} catch(error) {
    console.error("Failed to filter Theia resolutions. Cause: ", error);
    process.exit(2);
}

const depResolutions = listToResolutions(theiaResolutionsList);
depResolutions["**/inversify"] = "4.13.0"; // Remove this when https://github.com/theia-ide/theia/issues/3204 fixed
console.log(depResolutions);

PACKAGE_JSON["resolutions"] = depResolutions;
console.log(`Write generated resolutions to the package.json ${PACKAGE_JSON_PATH}`);
writeJsonToFile(PACKAGE_JSON_PATH, PACKAGE_JSON);

function resolutionExist(resolutionName) {
    try {
        console.log(`Check depedency ${resolutionName}`);
        const info = spawnSync('npm', ['view', `${resolutionName}@${THEIA_VERSION}`, 'version']);
        if (info.stdout.toString()) {
            return true;
        }
    } catch(error) {
        console.log(`Unable to check resolution with name ${resolutionName}`);
        process.exit(3);
    }
    return false;
}

/**
 * Convert resolutions list to resolutions object with required THEIA_VERSION.
 *
 * @param resolutionsList - resolutions list.
 */
function listToResolutions(resolutionsList) {
    const RESOLUTIONS = {};

    for (const resolution of resolutionsList) {
        if (resolutionExist(resolution)) {
            RESOLUTIONS[resolution] = THEIA_VERSION;
        }
    }

    return RESOLUTIONS;
}

/**
 * Write json to the file by path.
 *
 * @param filePath - file system location of the file.
 * @param json - json object to write.
 */
function writeJsonToFile(filePath, json) {
    const content = JSON.stringify(json, undefined, 4);
    fs.writeFileSync(filePath, content, 'utf8');
}
