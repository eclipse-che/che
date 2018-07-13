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
console.log(`Generate resolutions for ${PACKAGE_JSON_PATH}`);

const PACKAGE_JSON = require(PACKAGE_JSON_PATH);
const THEIA_VERSION = process.env.THEIA_VERSION;
const HOME = process.env.HOME;
const THEIA_DEP_PREFIX = "@theia/";

const theiaFullPackageJsonLink=`https://raw.githubusercontent.com/theia-ide/theia/v${THEIA_VERSION}/examples/browser/package.json`
const fullPackageJsonPath=`${HOME}/full-package.json`;

spawnSync('wget', ['-O', `${fullPackageJsonPath}`, `${theiaFullPackageJsonLink}`]);

if (!fs.existsSync(`${fullPackageJsonPath}`)) {
    console.log("Can't generate resolution, because we have not list all base Theia dependencies.");
    return;
}
const FULL_PACKAGE_JSON = require(`${fullPackageJsonPath}`);
const fullDependenciesList = Object.keys(FULL_PACKAGE_JSON['dependencies']);
const dependencies = listToResolutions(fullDependenciesList);
let ALL_RESOLUTIONS = {...dependencies};

const devDependencies = PACKAGE_JSON['devDependencies'];
const depsToGetResolutions = {...devDependencies, ...{"@theia/core": THEIA_VERSION}};
for (let dep in depsToGetResolutions) {
    const depResolsList = getResolutionsList(dep);
    depResolsList.push(dep);
    const depResolutions = listToResolutions(depResolsList);
    ALL_RESOLUTIONS = {...ALL_RESOLUTIONS, ...depResolutions};
}

console.log(`Generated resolutions: `, ALL_RESOLUTIONS);
PACKAGE_JSON["resolutions"] = ALL_RESOLUTIONS;

console.log(`Write generated resolutions to the package.json ${PACKAGE_JSON_PATH}`);
writeJsonToFile(PACKAGE_JSON_PATH, PACKAGE_JSON);

/**
 * Get list resolutions for Theia dependency.
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
        if (resolution.startsWith(THEIA_DEP_PREFIX)) {
            theiaResolutions.push(resolution);
        }
    }

    return theiaResolutions;
}

/**
 * Convert resolutions list to resolutions object with required THEIA_VERSION.
 *
 * @param resolutionsList - resolutions list.
 */
function listToResolutions(resolutionsList) {
    const RESOLUTIONS = {};

    for (const resolution of resolutionsList) {
        RESOLUTIONS[resolution] = THEIA_VERSION;
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
