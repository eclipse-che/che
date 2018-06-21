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
const cp = require("child_process");

const DEFAULT_THEIA_ROOT = '/home/default/theia';
const EXTENSIONS_DIR = '/home/default/theia-extensions';

const givenExtensions = process.argv;
// remove nodejs binary path
givenExtensions.shift();
// remove script name
givenExtensions.shift();

if (givenExtensions.length > 0) {
    cp.execSync(`mkdir -p ${EXTENSIONS_DIR}`);
    addExtensions(parseExtensions(givenExtensions));
}

/**
 * Converts given extensions args format to map:
 * name:git://github.com/user/extension.git to: name -> url
 * name:file:///path/to/extension to: name -> path
 */
function parseExtensions(givenExtensions) {
    const extensions = {};
    for (let extension of givenExtensions) {
        const colonPos = extension.indexOf(':');
        if (colonPos === -1) {
            console.error('Invalid extension format: ', extension);
            process.exit(1);
        }
        const extensionName = extension.substring(0, colonPos).trim();
        const extensionUri = extension.substring(colonPos + 1).trim();
        if (extensionUri.indexOf('://') === -1) {
            console.error('Invalid extension uri: ', extensionUri);
            process.exit(2);
        }
        extensions[extensionName] = extensionUri;
    }
    return extensions;
}

function addExtensions(extensions) {
    for (let extensionName in extensions) {
        const extensionRootPath = EXTENSIONS_DIR + '/' + extensionName.replace(/\//g, '_');
        const extensionUri = extensions[extensionName];
        if (extensionUri.startsWith('file://')) {
            const extensionPath = extensionUri.substring(7); // remove protocol
            if (!fs.existsSync(extensionPath)) {
                console.error('Invalid extension path: ', extensionPath);
                process.exit(2);
            }
            cp.execSync(`mv ${extensionPath} ${extensionRootPath}`);
        } else {
            cloneRepository(extensionRootPath, extensionUri);
        }
        buildExtension(extensionRootPath);
        extensions[extensionName] = getBinaryPath(extensionRootPath, extensionName);
    }
    addExtensionsIntoDefaultPackageJson(extensions);
}

function cloneRepository(path, url) {
    try {
        console.log('Cloning repository: ', url);
        cp.execSync(`git clone --depth=1 --quiet ${url} ${path}`);
    } catch (error) {
        console.error('Failed to clone repository: ', url);
        process.exit(3);
    }
}

function buildExtension(path) {
    try {
        console.log('Building extension: ', path);
        cp.execSync(`cd ${path} && yarn`);
    } catch (error) {
        console.error('Failed to build extension located in: ', path);
        process.exit(4);
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
    fs.writeFileSync(`${DEFAULT_THEIA_ROOT}/package.json`, JSON.stringify(theiaPackageJson), 'utf8');
}
