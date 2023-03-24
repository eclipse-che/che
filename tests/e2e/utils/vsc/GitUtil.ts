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

@injectable()
export class GitUtil {

    constructor() { }

    /**
     * Method extracts a test repo name from git clone https url;
     * it splits the url into string[] by "/" or ".", deletes empty elements and elements that contains just "git" word, than returns the last one;
     * please, avoid to call the test repo as just "git" or to use dots in the name, like: github.com/user/git.git, github.com/user/name.with.dots.
     * @param url git https url (which using for "git clone")
     * @return project name
     */
    getProjectNameFromGitUrl(url: string) {
        return url.split(/[\/.]/).filter((e: string) => e !== '' && e !== 'git').reverse()[0];
    }
}
