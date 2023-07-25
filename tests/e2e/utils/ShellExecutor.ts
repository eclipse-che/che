/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { echo, exec, ShellString } from 'shelljs';

export class ShellExecutor {
	static wait(seconds: number): void {
		this.execWithLog(`sleep ${seconds}s`);
	}

	static curl(link: string): ShellString {
		return this.execWithLog(`curl -k ${link}`);
	}

	protected static execWithLog(command: string): ShellString {
		echo(command);
		return exec(command);
	}
}
