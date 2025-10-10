/** *******************************************************************
 * copyright (c) 2019-2025 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { Logger } from './Logger';
import { injectable } from 'inversify';

@injectable()
export class StringUtil {
	/**
	 * extracts the project (repository) name from a Git HTTPS URL.
	 *
	 * Handles URLs from GitLab, GitHub, and Bitbucket by:
	 * - Removing query parameters and hash fragments;
	 * - Trimming branch or tree paths (e.g. `/tree/<branch>`, `/-/tree/<branch>`, `/src/<branch>`);
	 * - Stripping the `.git` suffix.
	 *
	 * ⚠️ Avoid naming repositories as `"git"` or using dots in names (`name.with.dots`),
	 * as they may be treated as separators.
	 *
	 * @param url Git HTTPS URL (as used for `git clone`)
	 * @returns Extracted project name, or an empty string if parsing fails
	 */
	static getProjectNameFromGitUrl(url: string): string {
		Logger.debug(`Original URL: ${url}`);

		try {
			// remove query and hash fragments
			url = url.split('?')[0].split('#')[0];

			// remove branch/tree path fragments for major providers
			url = url
				.replace(/\/-?\/tree\/[^/]+$/, '') // gitLab, GitHub
				.replace(/\/src\/[^/]+.*$/, ''); // bitbucket

			// take the last segment of the path and strip ".git"
			const projectName: string = (url.split('/').filter(Boolean).pop() || '').replace(/\.git$/, '');

			Logger.debug(`Extracted project name: ${projectName}`);
			return projectName;
		} catch (err) {
			Logger.error(`Failed to extract project name from URL ${url}: ${err}`);
			return '';
		}
	}

	static sanitizeTitle(arg: string): string {
		Logger.trace();

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
		Logger.trace();

		return command.replace(/[{}]/g, '').replace(/(?<!")\${?[a-zA-Z0-9_+\-\s]+\b}?/gm, '"$&"');
	}

	/**
	 * replaces the cookie value of the specified cookie
	 * @param cookie cookie names and values, seperated with ;
	 * @param name name of cookie to replace its value for
	 * @param replaceStr the new value of the cookie
	 * @return updated cookie string with the cookie value replaced
	 */
	static updateCookieValue(cookie: string, name: string, replaceStr: string): string {
		Logger.trace();

		const regex: RegExp = new RegExp(`(${name})=[^;]+`, 'g');
		return cookie.replace(regex, `$1=${replaceStr}`);
	}

	/**
	 * replaces the query value of the specified query
	 * @param queryString query string (ie. query=value&query2=value2)
	 * @param name name of the query to replace
	 * @param replaceStr new query value
	 * @returns updated queryString with the query value replaced
	 */
	static updateUrlQueryValue(queryString: string, name: string, replaceStr: string): string {
		Logger.trace();

		const regex: RegExp = new RegExp(`(${name})=[^&]+`, 'g');
		return queryString.replace(regex, `$1=${replaceStr}`);
	}
}
