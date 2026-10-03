/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { spawnSync } from 'child_process';

export function getOpenShiftApiUrl(applicationUrl: string): string {
	const url: URL = new URL(applicationUrl);
	const match: RegExpMatchArray | null = url.hostname.match(/^(?:devspaces|eclipse-che|console-openshift-console)\.apps\.(.+)$/);
	if (!match) {
		throw new Error('Cannot derive the OpenShift API from the application URL; set OCP_API_URL explicitly.');
	}
	return `https://api.${match[1]}:${match[1].endsWith('p3.openshiftapps.com') ? '443' : '6443'}`;
}

export function isApiReachable(url: string, direct: boolean = false): boolean {
	// do not use --fail: 401/403 responses mean the transport works, not a port failure.
	const args: string[] = ['--silent', '--insecure', '--connect-timeout', '5', '--max-time', '10', '--output', '/dev/null'];
	if (direct) {
		args.push('--noproxy', '*');
	}
	args.push(`${url.replace(/\/$/, '')}/version`);
	return spawnSync('curl', args, { stdio: 'ignore', timeout: 15000 }).status === 0;
}

export function bypassApiProxy(clusterHost: string): void {
	const entries: string = `localhost,127.0.0.1,.crw-qe.com,.openshiftapps.com,${clusterHost},.${clusterHost}`;
	const combined: string = Array.from(
		new Set(
			[process.env.NO_PROXY, process.env.no_proxy, entries]
				.filter(Boolean)
				.join(',')
				.split(',')
				.map((entry: string): string => entry.trim())
				.filter(Boolean)
		)
	).join(',');
	process.env.NO_PROXY = combined;
	process.env.no_proxy = combined;
}

export function resolveOpenShiftApiUrl(primary: string, reachable: (url: string, direct?: boolean) => boolean = isApiReachable): string {
	const url: URL = new URL(primary);
	if (url.hostname.startsWith('api-443.apps.')) {
		bypassApiProxy(url.hostname.substring('api-443.apps.'.length));
		return primary;
	}
	if (url.hostname.startsWith('api.')) {
		bypassApiProxy(url.hostname.substring('api.'.length));
	}
	if (!url.hostname.startsWith('api.') || url.port !== '6443' || reachable(primary, true)) {
		return primary;
	}
	const clusterHost: string = url.hostname.substring('api.'.length);
	const fallback: string = `https://api-443.apps.${clusterHost}:443`;
	if (reachable(fallback, true)) {
		bypassApiProxy(clusterHost);
		return fallback;
	}
	return primary;
}
