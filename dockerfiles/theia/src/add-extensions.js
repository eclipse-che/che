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

const DEFAULT_THEIA_ROOT = process.env.HOME;
const EXTENSIONS_DIR = `${DEFAULT_THEIA_ROOT}/extensions`;
const EXTENSION_FILE = 'extensions.json';
const EXTENSIONS_FILE_PATH = `${DEFAULT_THEIA_ROOT}/${EXTENSION_FILE}`;

const EXTENSION_TYPE_GIT = "git";
const EXTENSION_TYPE_DIR = "dir";

if (!fs.existsSync(EXTENSIONS_FILE_PATH)) {
    console.error(`${EXTENSIONS_FILE_PATH} was not found.`);
    process.exit(1);
}

const givenExtensions = require(EXTENSIONS_FILE_PATH)["extensions"];
if (givenExtensions.length > 0) {
    spawnSync(`mkdir -p ${EXTENSIONS_DIR}`);
    addExtensions(givenExtensions);
}

function addExtensions(extensions) {
    const extensionsToAdd = {};
    for (let extension of extensions) {
        const extensionName = extension["name"];
        let extensionRootPath = EXTENSIONS_DIR + '/' + extensionName.replace(/\//g, '_');

        const extensionType = extension["type"];
        switch(extensionType) {
            case EXTENSION_TYPE_DIR:
                addExtensionFromDir(extensionRootPath, extension);
                break;
            case EXTENSION_TYPE_GIT:
                addExtensionFromGit(extensionRootPath, extension);
                break;
            default:
                throw new Error(`Invalid extension type ${extensionType}.`);
        }
        const buildFolder = extension["folder"];
        const originalExtensionPath = extensionRootPath;
        if (buildFolder) {
            console.log('There is a build folder defined', buildFolder);
            extensionRootPath = extensionRootPath + '/' + buildFolder;
        }
        buildExtension(extensionRootPath, originalExtensionPath);
        extensionsToAdd[extensionName] = 'file://' + extensionRootPath;
    }
    console.log("Extension to add: ", extensionsToAdd);
    addExtensionsIntoDefaultPackageJson(extensionsToAdd);
}

function addExtensionFromDir(extensionRootPath, extension) {
    const extensionSource = extension["source"];
    if (!fs.existsSync(extensionSource)) {
        console.error('Invalid extension path: ', extensionSource);
        process.exit(2);
    }
    spawnSync(`mv ${extensionSource} ${extensionRootPath}`);
}

function addExtensionFromGit(extensionRootPath, extension) {
    const checkoutTarget = extension["checkoutTo"];
    const extensionSource = extension["source"];
    cloneRepository(extensionRootPath, extensionSource);

    checkoutRepo(extensionRootPath, checkoutTarget);
}

function cloneRepository(path, url) {
    try {
        console.log(`>>> Cloning repository: ${url}`);
        spawnSync(`git`, ['clone', `${url}`, `${path}`], {stdio:[0,1,2]});
    } catch (error) {
        console.error(`Failed to clone repository: ${url}`, error);
        process.exit(3);
    }
}

function checkoutRepo(path, checkoutTarget) {
    try {
         if (!checkoutTarget) {
            console.error(`Field 'checkoutTo' is required for git extensions. Path ${path}`);
            process.exit(4);
        }
        console.log(`>>> Checkout repository to: ${checkoutTarget}`);

        spawnSync('git', ['checkout', checkoutTarget], {cwd: `${path}`, stdio:[0,1,2]});
    } catch (error) {
        console.error(`Failed to checkout repository to branch: ${checkoutTarget}`, error);
        process.exit(5);
    }
}

function buildExtension(path, rootPath) {
    try {
        console.log('Generate versions for extension: ', path);
        spawnSync(`${DEFAULT_THEIA_ROOT}/versions.sh`, [], {cwd: `${path}`});
        if (path !== rootPath) {
            console.log("Removing parent files");
            // cleanup files in root path
            fs.unlinkSync(rootPath + '/package.json');
            fs.unlinkSync(rootPath + '/lerna.json');
        }
        console.log('Building extension: ', path);
        const nodeModulesPath = `${DEFAULT_THEIA_ROOT}/node_modules`;
        // build extension, but use Theia node_modules to reuse dependencies and prevent growing docker image.
        spawnSync(`yarn`, ['--modules-folder', nodeModulesPath, '--global-folder', nodeModulesPath], {cwd: `${path}`, stdio:[0,1,2]});
    } catch (error) {
        console.error(error);
        console.error('Failed to build extension located in: ', path);
        process.exit(6);
    }
}


function addExtensionsIntoDefaultPackageJson(extensions) {
    let theiaPackageJson = require(`${DEFAULT_THEIA_ROOT}/package.json`);
    let dependencies = theiaPackageJson['dependencies'];
    for (let extension in extensions) {
        dependencies[extension] = extensions[extension];
    }
    theiaPackageJson['dependencies'] = dependencies;
    const json = JSON.stringify(theiaPackageJson, undefined, 4);
    fs.writeFileSync(`${DEFAULT_THEIA_ROOT}/package.json`, json, 'utf8');
}
