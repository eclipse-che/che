"use strict";
/*
 * Copyright (c) 2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
const fs = require("fs");
const spawnSync = require("child_process").spawnSync;

const PACKAGE_JSON_PATH = process.argv[2];
const RESOLUTIONS_PATH = process.argv[3]; 
console.log(`Generate resolutions from ${PACKAGE_JSON_PATH}`);

const PACKAGE_JSON = require(PACKAGE_JSON_PATH);
const THEIA_VERSION = process.env.THEIA_VERSION;
const THEID_DEP_PREFIX = "@theia/";

const dependencies = PACKAGE_JSON['dependencies'];
const devDependencies = PACKAGE_JSON['devDependencies'];
let ALL_RESOLUTIONS = {...dependencies};

const depsToGetResolutions = {...devDependencies, ...{"@theia/core": THEIA_VERSION}};
for (let dep in depsToGetResolutions) {
    const depResolsList = getResolutionsList(dep);
    depResolsList.push(dep);
    const depResolutions = listToResolutions(depResolsList);
    ALL_RESOLUTIONS = {...ALL_RESOLUTIONS, ...depResolutions};
}

console.log(`Generated resolutions: `, ALL_RESOLUTIONS);
writeJsonToFile(RESOLUTIONS_PATH, ALL_RESOLUTIONS);

/**
 * Get list resolutions for theia dependency.
 *
 * @param theiaDep - theia dependency
 */
function getResolutionsList(theiaDep) {
    console.info(`Get resolutions for ${theiaDep} ...`);

    const depToSearch = theiaDep + `@` + THEIA_VERSION;
    const dependencies = spawnSync('npm', ['view', depToSearch, 'dependencies', '--json']);

    const resolutionsJson = JSON.parse(String(dependencies.stdout));

    return filterResolutionsList(Object.keys(resolutionsJson));
}

/**
 * Return only Theia resolutions list.
 * 
 * @param resolutions - resolutions to filter.
 */
function filterResolutionsList(resolutions) {
    const theiaResolutions = [];

    for (const resolution of resolutions) {
        if (resolution.startsWith(THEID_DEP_PREFIX)) {
            theiaResolutions.push(resolution);
        }
    }

    return theiaResolutions;
}

/**
 * Convert resolutions list to resolutions object with required versions.
 *
 * @param resolutionsList - resolutions list.
 */
function listToResolutions(resolutionsList) {
    const RESOLUTIONS = {}

    for (const resolution of resolutionsList) {
        ALL_RESOLUTIONS[resolution] = THEIA_VERSION;
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
