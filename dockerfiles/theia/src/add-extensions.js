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
        const extensionRootPath = EXTENSIONS_DIR + '/' + extensionName.replace(/\//g, '_');

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
        buildExtension(extensionRootPath);
        extensionsToAdd[extensionName] = getBinaryPath(extensionRootPath, extensionName);
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
    const checkoutTarget = extension["version"];
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
            checkoutTarget = 'master';
        }
        console.log(`>>> Checkout repository to: ${checkoutTarget}`);

        spawnSync('git', ['checkout', checkoutTarget], {cwd: `${path}`, stdio:[0,1,2]});
    } catch (error) {
        console.error(`Failed to checkout repository to branch: ${checkoutTarget}`, error);
        process.exit(4);
    }
}

function buildExtension(path) {
    try {
        console.log('Building extension: ', path);
        const nodeModulesPath = `${DEFAULT_THEIA_ROOT}/node_modules`;
        // build extension, but use Theia node_modules to reuse dependencies and prevent growing docker image.
        spawnSync(`yarn`, ['--modules-folder', nodeModulesPath, '--global-folder', nodeModulesPath, '--cache-folder', nodeModulesPath], {cwd: `${path}`, stdio:[0,1,2]});
    } catch (error) {
        console.error('Failed to build extension located in: ', path);
        process.exit(5);
    }
}

function getBinaryPath(extensionRoot, extensionName) {
    const rootPackageJson = require(`${extensionRoot}/package.json`);
    if ('theiaExtensions' in rootPackageJson) {
        return extensionRoot;
    }

    const dirs = fs.readdirSync(extensionRoot).filter(item => !item.startsWith('.') && fs.lstatSync(extensionRoot + '/' + item).isDirectory());
    for (let dirName of dirs) {
        const extensionTargetDir = extensionRoot + '/' + dirName;
        const packageJsonPath = extensionTargetDir + '/package.json';
        if (fs.existsSync(packageJsonPath)) {
            let packageJson = require(packageJsonPath);
            if (packageJson['name'] === extensionName) {
                return extensionRoot + '/' + dirName;
            }
        }
    }
    console.error('Failed to find folder with binaries for extension: ', extensionRoot);
    process.exit(5);
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
