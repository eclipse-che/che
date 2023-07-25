/** *******************************************************************
 * copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable } from 'inversify';
import { Logger } from './Logger';

@injectable()
export class StringUtil {
	/**
	 * method extracts a test repo name from git clone https url;
	 * it splits the url into string[] by "/" or ".", deletes empty elements and elements that contains just "git", "main" or "tree" word, than returns the last one;
	 * please, avoid to call the test repo as just "git" or to use dots in the name, like: github.com/user/git.git, github.com/user/name.with.dots.
	 * @param url git https url (which using for "git clone")
	 * @return project name
	 */
	static getProjectNameFromGitUrl(url: string): string {
		Logger.debug(`${url}`);
		if (url.includes('?')) {
			url = url.substring(0, url.indexOf('?'));
		}
		if (url.includes('/tree/')) {
			url = url.split('/').slice(0, -2).join('/');
		}
		const projectName: string = url
			.split(/[\/.]/)
			.filter((e: string): boolean => !['git', ''].includes(e))
			.reverse()[0];
		Logger.debug(`${projectName}`);
		return projectName;
	}

	static sanitizeTitle(arg: string): string {
		return arg
			.replace(/\//g, '+')
			.replace(/,/g, '.')
			.replace(/:/g, '-')
			.replace(/['"]/g, '')
			.replace(/[^a-z0-9+\-.()\[\]_]/gi, '_');
	}

	/**
	 * replaces ${ENV}, $ENV to "$ENV"
	 * @param command string command with environmental variables in unsupported format
	 * @return updated command with environmental variables in supported format
	 */

	static updateCommandEnvsToShStyle(command: string): string {
		return command.replace(/[{}]/g, '').replace(/(?<!")\${?[a-zA-Z0-9_+\-\s]+\b}?/gm, '"$&"');
	}
}
