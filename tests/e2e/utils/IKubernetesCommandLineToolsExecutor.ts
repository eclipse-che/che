/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { ShellString } from 'shelljs';

export interface IKubernetesCommandLineToolsExecutor {
	loginToOcp(): void;

	getContainerName(): string;

	getWorkspacePodName(): string;

	deleteDevWorkspace(): void;

	applyAndWaitDevWorkspace(yamlConfiguration: string): ShellString;

	executeCommand(commandToExecute: string, container: string): ShellString;

	applyYamlConfigurationAsStringOutput(yamlConfiguration: string): ShellString;

	applyYamlConfigurationAsFile(pathToFile: string): void;

	getDevWorkspaceYamlConfiguration(): ShellString;

	waitDevWorkspace(timeout: number): ShellString;

	createProject(projectName: string): void;

	deleteProject(projectName: string): void;

	getPodAndContainerNames(): void;

	isUserLoggedIn(user: string): boolean;

	getServerUrl(): string;
}
