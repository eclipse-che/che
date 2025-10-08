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
	 * The method:
	 * - Removes query parameters (`?…`) and hash fragments (`#…`);
	 * - Trims branch or tree paths (e.g. `/tree/<branch>` or `/-/tree/<branch>`);
	 * - Splits the cleaned URL into parts by "/" or ".";
	 * - Filters out empty parts and generic words like `"git"`;
	 * - Returns the last remaining part — the actual project name.
	 *
	 * ⚠️ Notes:
	 * - Avoid naming repositories as just `"git"`;
	 * - Avoid using dots in the repo name (e.g. `name.with.dots`), since they may be treated as separators.
	 *
	 * @param url Git HTTPS URL (same as used for `git clone`)
	 * @returns The extracted project name, or an empty string if parsing fails
	 */
	static getProjectNameFromGitUrl(url: string): string {
		Logger.debug(`Original URL: ${url}`);

		try {
			// remove query and hash fragments
			url = url.split('?')[0].split('#')[0];

			// remove branch/tree parts for GitLab, GitHub, Bitbucket
			url = url
				.replace(/\/-?\/tree\/[^/]+$/, '') // gitLab, GitHub
				.replace(/\/src\/[^/]+.*$/, ''); // bitbucket

			// extract last part of the path
			let projectName: string = url.split('/').filter(Boolean).pop() || '';

			// remove .git if present
			projectName = projectName.replace(/\.git$/, '');

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
