/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { exec, ShellString } from 'shelljs';
import { Logger } from './Logger';
import { injectable } from 'inversify';

@injectable()
export class ShellExecutor {
	wait(seconds: number): void {
		this.executeCommand(`sleep ${seconds}s`);
	}

	curl(link: string): ShellString {
		return this.executeCommand(`curl -k ${link}`);
	}

	executeCommand(command: string): ShellString {
		Logger.debug(command);
		return exec(command);
	}
}
