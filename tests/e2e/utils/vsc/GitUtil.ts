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
import YAML from 'yaml';
import { Logger } from '../Logger';
import axios, { AxiosResponse } from 'axios';

@injectable()
export class GitUtil {
    /**
     * Method extracts a test repo name from git clone https url;
     * it splits the url into string[] by "/" or ".", deletes empty elements and elements that contains just "git" word, than returns the last one;
     * please, avoid to call the test repo as just "git" or to use dots in the name, like: github.com/user/git.git, github.com/user/name.with.dots.
     * @param url git https url (which using for "git clone")
     * @return project name
     */
    static getProjectNameFromGitUrl(url: string): string {
        Logger.debug(`${this.constructor.name}.${this.getProjectNameFromGitUrl.name} - ${url}`);
        const projectName: string = url.split(/[\/.]/).filter((e: string) => e !== '' && e !== 'git').reverse()[0];
        Logger.debug(`${this.constructor.name}.${this.getProjectNameFromGitUrl.name} - ${projectName}`);
        return projectName;
    }
    /**
     * Method extracts a git url for DevWorkspace configuration from meta.yaml file;
     * @param linkToMetaYaml raw url to git repository where meta.yaml is;
     * @return git link which uses in DevWorkspaceConfigurationHelper as DevfileUrl parameter
     */
     static async getProjectGitLinkFromLinkToMetaYaml(linkToMetaYaml: string): Promise<string> {
        Logger.debug(`${this.constructor.name}.${this.getProjectGitLinkFromLinkToMetaYaml.name} - ${linkToMetaYaml}`);
        const response: AxiosResponse = await axios.get(linkToMetaYaml);
        const metaYamlContent: any = YAML.parse(response.data);
        Logger.debug(`${this.constructor.name}.${this.getProjectGitLinkFromLinkToMetaYaml.name} - ${metaYamlContent.links.v2}`);
        return metaYamlContent.links.v2;
    }
}
