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

const defaultTheiaRoot = '/home/default/theia';
const pluginsDir = '/home/default/theia-plugins';

const pluginsUrls = process.argv;
// remove nodejs binary path
pluginsUrls.shift();
// remove script name
pluginsUrls.shift();

if (pluginsUrls.length > 0) {
    addPlugins(parsePlugins(pluginsUrls));
}

/** Converts plugin args format from name:git://github.com/user/plugin.git to map: name -> url */
function parsePlugins(pluginsUrls) {
    const plugins = {};
    for (let plugin of pluginsUrls) {
        const colonPos = plugin.indexOf(':');
        if (colonPos === -1) {
            console.error('Invalid plugin format: ', plugin);
            process.exit(1);
        }
        const pluginName = plugin.substring(0, colonPos).trim();
        const pluginUrl = plugin.substring(colonPos + 1).trim();
        if (pluginUrl.indexOf('://') === -1) {
            console.error('Invalid plugin repository: ', pluginUrl);
            process.exit(2);
        }
        plugins[pluginName] = pluginUrl;
    }
    return plugins;
}

/** Adds git hosted plugins. */
function addPlugins(plugins) {
    for (let pluginName in plugins) {
        const pluginRootPath = pluginsDir + '/' + pluginName;
        cloneRepository(pluginRootPath, plugins[pluginName]);
        buildPlugin(pluginRootPath);
        plugins[pluginName] = getBinaryPath(pluginRootPath, pluginName);
    }
    addPluginsIntoDefaultPackageJson(plugins);
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

function buildPlugin(path) {
    try {
        console.log('Building plugin: ', path);
        cp.execSync(`cd ${path} && yarn`);
    } catch (error) {
        console.error('Failed to build plugin located in: ', path);
        process.exit(4);
    }
}

function getBinaryPath(pluginRoot, pluginName) {
    const rootPackageJson = require(`${pluginRoot}/package.json`);
    if ('theiaExtensions' in rootPackageJson) {
        return pluginRoot;
    }

    const dirs = fs.readdirSync(pluginRoot).filter(item => !item.startsWith('.') && fs.lstatSync(pluginRoot + '/' + item).isDirectory());
    for (let dirName of dirs) {
        const pluginTargetDir = pluginRoot + '/' + dirName;
        const packageJsonPath = pluginTargetDir + '/package.json';
        if (fs.existsSync(packageJsonPath)) {
            let packageJson = require(packageJsonPath);
            if (packageJson['name'] === pluginName) {
                return pluginRoot + '/' + dirName;
            }
        }
    }
    console.error('Failed to find folder with binaries for plugin: ', pluginRoot);
    process.exit(5);
}

function addPluginsIntoDefaultPackageJson(plugins) {
    let theiaPackageJson = require(`${defaultTheiaRoot}/package.json`);
    let dependencies = theiaPackageJson['dependencies'];
    for (let plugin in plugins) {
        dependencies[plugin] = plugins[plugin];
    }
    theiaPackageJson['dependencies'] = dependencies;
    fs.writeFileSync(`${defaultTheiaRoot}/package.json`, JSON.stringify(theiaPackageJson), 'utf8');
}
