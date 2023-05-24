/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable } from 'inversify';
import { Logger } from './Logger';
import { KubernetesCommandLineToolsExecutor } from './KubernetesCommandLineToolsExecutor';

@injectable()
export class StringUtil {
    /**
     * Method extracts a test repo name from git clone https url;
     * it splits the url into string[] by "/" or ".", deletes empty elements and elements that contains just "git", "main" or "tree" word, than returns the last one;
     * please, avoid to call the test repo as just "git" or to use dots in the name, like: github.com/user/git.git, github.com/user/name.with.dots.
     * @param url git https url (which using for "git clone")
     * @return project name
     */
    static getProjectNameFromGitUrl(url: string): string {
        Logger.debug(`${this.constructor.name}.${this.getProjectNameFromGitUrl.name} - ${url}`);
        if (url.includes('/tree/')) {
            url = url.split('/').slice(0, -2).join('/');
        }
        const projectName: string = url.split(/[\/.]/).filter((e: string) => !['git', ''].includes(e)).reverse()[0];
        Logger.debug(`${this.constructor.name}.${this.getProjectNameFromGitUrl.name} - ${projectName}`);
        return projectName;
    }

    /**
     * Uses in DevfileAcceptanceTestAPI.spec.ts to get full path to execute build command without unknown environmental variable
     * @param pathWithUnknownEnvVariable command.exec.workingDir from devfileContext.devfile
     * @param containerTerminal instance of KubernetesCommandLineToolsExecutor.ContainerTerminal
     * @return full path of imported project working dir in workspace
     */
    static getFullWorkingDirPathExplicit(pathWithUnknownEnvVariable: string, containerTerminal: KubernetesCommandLineToolsExecutor.ContainerTerminal): string {
        const envVariable: { name: string; value: string } = {
            name: '',
            value: ''
        };

        envVariable.name = pathWithUnknownEnvVariable.substring(
            pathWithUnknownEnvVariable.indexOf('{') + 1,
            pathWithUnknownEnvVariable.lastIndexOf('}'));

        if (pathWithUnknownEnvVariable.includes('/')) {
            pathWithUnknownEnvVariable = pathWithUnknownEnvVariable.substring(pathWithUnknownEnvVariable.indexOf('/'));
        }

        envVariable.value = containerTerminal.getEnvValue(envVariable.name);
        return envVariable.value + pathWithUnknownEnvVariable;
    }

    static sanitizeTitle(arg: string): string {
        return arg.replace(/\//g, '+').replace(/,/g, '.').replace(/:/g, '-').replace(/['"]/g, '').replace(/[^a-z0-9+\-.()\[\]_]/gi, '_');
    }
}
