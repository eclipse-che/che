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

const theiaRoot = '/home/theia';
const defaultTheiaRoot = '/home/default/theia';
const gitPluginsRoot = '/tmp/theia-plugins-from-git-repos';

const DEFAULT_THEIA_PORT = 3000;
const THEIA_PORT = getTheiaPort();

prepareTheia();

/** Rebuilds if needed and runs Theia */
function prepareTheia() {
    process.chdir(theiaRoot);

    const extraPlugins = getExtraTheiaPlugins();
    let currentPlugins = getTheiaPlugins();
    let newPlugins = getDefaultTheiaPlugins();

    if (Object.keys(currentPlugins).length === 0) {
        copyDefaultTheiaBuild();
        currentPlugins = getTheiaPlugins();
    }

    for (let pluginName in extraPlugins) {
        newPlugins[pluginName] = extraPlugins[pluginName];
    }

    if (!isPluginsEqual(currentPlugins, newPlugins)) {
        console.log('Plugins set changed. Rebuilding Theia...');
        rebuildTheiaWithNewPluginsAndRun(newPlugins);
    } else {
        handleError(callRun());
    }
}

function isPluginsEqual(pls1, pls2) {
    if (Object.keys(pls1).length !== Object.keys(pls2).length) {
        return false;
    }

    for (let dep in pls1) {
        if (pls1[dep] !== pls2[dep]) {
            return false;
        }
    }

    return true;
}

function copyDefaultTheiaBuild() {
    cp.execSync(`cp -r ${defaultTheiaRoot}/node_modules ${theiaRoot} && cp -r ${defaultTheiaRoot}/src-gen ${theiaRoot} && cp -r ${defaultTheiaRoot}/lib ${theiaRoot} && cp ${defaultTheiaRoot}/yarn.lock ${defaultTheiaRoot}/package.json ${theiaRoot}`);
}

function rebuildTheiaWithNewPluginsAndRun(newPlugins) {
    let theiaPackageJson = require(`${theiaRoot}/package.json`);
    theiaPackageJson['dependencies'] = newPlugins;
    fs.writeFileSync(`${theiaRoot}/package.json`, JSON.stringify(theiaPackageJson), 'utf8');
    cp.execSync(`rm -rf ${theiaRoot}/src-gen`);

    handleError(callYarn()
        .then(callBuild).catch(error => {
            // invalidate broken build and force rebuild next time
            cp.execSync(`rm -rf ${theiaRoot}/package.json`);
            throw error;
        })
        .then(callRun));
}

/** Returns standard (default) Theia dependencies */
function getDefaultTheiaPlugins() {
    const defaultTheiaPackageJson = require(`${defaultTheiaRoot}/package.json`);
    return defaultTheiaPackageJson['dependencies'];
}

/** Returns current (after previous launch) Theia plugins or empty object on first start */
function getTheiaPlugins() {
    if (!fs.existsSync(`${theiaRoot}/package.json`)) {
        return {};
    }
    const theiaPackageJson = require(`${theiaRoot}/package.json`);
    return theiaPackageJson['dependencies'];
}

/** Returns extra Theia plugins which is set via THEIA_PLUGINS env variable */
function getExtraTheiaPlugins() {
    let plugins = {};
    const currentPluginsString = process.env.THEIA_PLUGINS;
    if (currentPluginsString) {
        for (let plugin of currentPluginsString.split(',')) {
            plugin = plugin.trim();
            if (plugin === '') {
                continue;
            }
            const colonPos = plugin.indexOf(':');
            if (colonPos !== -1) {
                const pluginName = plugin.substring(0, colonPos).trim();
                const pluginVersion = plugin.substring(colonPos + 1).trim();
                if (pluginVersion.startsWith('git://') || pluginVersion.startsWith('https://')) {
                    addPluginFromGitRepository(plugins, pluginName, pluginVersion);
                } else {
                    plugins[pluginName] = pluginVersion;
                }
            } else {
                plugins[plugin] = 'latest';
            }
        }
    }
    return plugins;
}

/**
 * Clones git repository and adds the plugin as local.
 * This is done becouse Theia requires npm package.json (not lerna) which is located, in most cases, in subdirectory of repository.
 */
function addPluginFromGitRepository(plugins, pluginName, gitRepository) {
    const pluginPath = gitPluginsRoot + '/' + pluginName + '/';
    try {
        cp.execSync(`git clone --depth=1 --quiet ${gitRepository} ${pluginPath}`);
    } catch (error) {
        // failed to clone repository
        try {
            cp.execSync(`git clone --depth=1 --quiet ${gitRepository} ${pluginPath}`);
        } catch (error) {
            // failed again, skip plugin
            console.error('Failed to get plugin: ' + pluginName + '. Skipping... Is the url specified properly?');
            return;
        }
    }

    const rootPackageJson = require(pluginPath + 'package.json');
    if (rootPackageJson['name'] === pluginName && !fs.existsSync(pluginPath + 'lerna.json')) {
        plugins[pluginName] = gitRepository;
    } else {
        const dirs = fs.readdirSync(pluginPath).filter(item => !item.startsWith('.') && fs.lstatSync(pluginPath + item).isDirectory());
        for (let dirName of dirs) {
            const pluginTargetDir = pluginPath + dirName;
            const packageJsonPath = pluginTargetDir + '/package.json';
            if (fs.existsSync(packageJsonPath)) {
                let packageJson = require(packageJsonPath);
                if (packageJson['name'] === pluginName) {
                    if (!fs.existsSync(pluginTargetDir + '/node_mudules') || !fs.existsSync(pluginTargetDir + '/lib')) {
                        try {
                            console.log('Building plugin: ' + pluginName);
                            cp.execSync(`cd ${pluginTargetDir} && yarn`);
                        } catch (error) {
                            console.error('Skipping ' + pluginName + ' plugin because of following error: ' + error);
                            return;
                        }
                    }
                    plugins[pluginName] = pluginTargetDir;
                    return;
                }
            }
        }
        console.error(pluginName + ' is not valid plugin. Skipping.');
    }
}

function promisify(command, p) {
    return new Promise((resolve, reject) => {
        p.stdout.on('data', data => process.stdout.write(data.toString()));
        p.stderr.on('data', data => process.stderr.write(data.toString()));
        p.on('error', reject);
        p.on('close', code => {
            if (code === 0) {
                resolve();
            }
            else {
                reject(new Error(command + ' failed with the exit code ' + code));
            }
        });
    });
}
function callYarn() {
    return promisify('yarn', cp.spawn('yarn'));
}
function callBuild() {
    return promisify('yarn theia build', cp.spawn('yarn', ['theia', 'build']));
}
function callRun() {
    return promisify('yarn theia start', cp.spawn('yarn', ['theia', 'start', '/projects', '--hostname=0.0.0.0', "--port=" + THEIA_PORT]));
}
function handleError(p) {
    p.catch(error => {
        console.error(error);
    }).catch(() => {
    });
}

/**
 * Returns port on which Theia should be run.
 * If invalid or not specified, then default port will be returned.
 */
function getTheiaPort() {
    const port = Number(process.env.THEIA_PORT);
    if (port) {
        return port;
    }
    return DEFAULT_THEIA_PORT;
}
