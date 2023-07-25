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
import { Logger } from './Logger';
import { ShellExecutor } from './ShellExecutor';
import * as path from 'path';
import { API_TEST_CONSTANTS, KubernetesCommandLineTool } from '../constants/API_TEST_CONSTANTS';
import { BASE_TEST_CONSTANTS } from '../constants/BASE_TEST_CONSTANTS';
import { OAUTH_CONSTANTS } from '../constants/OAUTH_CONSTANTS';

export class KubernetesCommandLineToolsExecutor extends ShellExecutor {
	private static container: string;
	private static pod: string;
	private readonly namespace: string;
	private readonly workspaceName: string | undefined;
	private readonly KUBERNETES_COMMAND_LINE_TOOL: string = API_TEST_CONSTANTS.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL;

	constructor(_workspaceName?: string, _namespace?: string) {
		super();
		this.workspaceName = _workspaceName;
		this.namespace = this.setNamespace(_namespace);
	}

	get getWorkspaceName(): string {
		return <string>this.workspaceName;
	}

	get getNamespace(): string {
		return this.namespace;
	}

	// login to Openshift cluster with username and password
	loginToOcp(): void {
		if (this.KUBERNETES_COMMAND_LINE_TOOL === KubernetesCommandLineTool.OC) {
			Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - login to the "OC" client.`);
			const url: string = this.getServerUrl();
			if (this.isUserLoggedIn()) {
				Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - user already logged`);
			} else {
				Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - login ${url}, ${OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME}`);
				exec(
					`oc login --server=${url} -u=${OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME} -p=${OAUTH_CONSTANTS.TS_SELENIUM_OCP_PASSWORD} --insecure-skip-tls-verify`
				);
			}
		} else {
			Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - doesn't support login command`);
		}
	}

	getContainerName(): string {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - get container name.`);
		const output: ShellString = ShellExecutor.execWithLog(
			`${this.KUBERNETES_COMMAND_LINE_TOOL} get ${KubernetesCommandLineToolsExecutor.pod} -o jsonpath='{.spec.containers[*].name}' -n ${this.namespace}`
		);
		echo('\n');
		return output.stderr ? output.stderr : output.stdout;
	}

	getWorkspacePodName(): string {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - get workspace pod name.`);
		const output: ShellString = ShellExecutor.execWithLog(
			`${this.KUBERNETES_COMMAND_LINE_TOOL} get pod -l controller.devfile.io/devworkspace_name=${this.workspaceName} -n ${this.namespace} -o name`
		);
		return output.stderr ? output.stderr : output.stdout.replace('\n', '');
	}

	deleteDevWorkspace(): void {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - delete '${this.workspaceName}' workspace`);
		ShellExecutor.execWithLog(
			`${this.KUBERNETES_COMMAND_LINE_TOOL} patch dw ${this.workspaceName} -n ${this.namespace} -p '{ "metadata": { "finalizers": null }}' --type merge || true`
		);
		ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} delete dw ${this.workspaceName} -n ${this.namespace} || true`);
		ShellExecutor.execWithLog(
			`${this.KUBERNETES_COMMAND_LINE_TOOL} delete dwt ${BASE_TEST_CONSTANTS.TS_SELENIUM_EDITOR}-${this.workspaceName} -n ${this.namespace} || true`
		);
	}

	applyAndWaitDevWorkspace(yamlConfiguration: string): ShellString {
		if (this.KUBERNETES_COMMAND_LINE_TOOL === KubernetesCommandLineTool.KUBECTL) {
			this.createNamespace();
		}
		this.applyYamlConfigurationAsStringOutput(yamlConfiguration);
		return this.waitDevWorkspace();
	}

	executeCommand(commandToExecute: string, container: string = KubernetesCommandLineToolsExecutor.container): ShellString {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL}`);
		return ShellExecutor.execWithLog(
			`${this.KUBERNETES_COMMAND_LINE_TOOL} exec -i ${KubernetesCommandLineToolsExecutor.pod} -n ${this.namespace} -c ${container} -- sh -c '${commandToExecute}'`
		);
	}

	applyYamlConfigurationAsStringOutput(yamlConfiguration: string): ShellString {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL}`);
		return ShellExecutor.execWithLog(
			`cat <<EOF | ${this.KUBERNETES_COMMAND_LINE_TOOL} apply -n ${this.namespace} -f - \n` + yamlConfiguration + '\n' + 'EOF'
		);
	}

	applyYamlConfigurationAsFile(pathToFile: string): void {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL}`);
		ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} apply -n ${this.namespace} -f "${path.resolve(pathToFile)}"`);
	}

	getDevWorkspaceYamlConfiguration(): ShellString {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL}`);
		return ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} get dw ${this.workspaceName} -n ${this.namespace} -o yaml`);
	}

	waitDevWorkspace(timeout: number = 360): ShellString {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - wait till workspace ready.`);
		const output: ShellString = ShellExecutor.execWithLog(
			`${this.KUBERNETES_COMMAND_LINE_TOOL} wait -n ${this.namespace} --for=condition=Ready dw ${this.workspaceName} --timeout=${timeout}s`
		);
		this.getPodAndContainerNames();
		return output;
	}

	createNamespace(): void {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - create namespace "${this.namespace}".`);
		ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} create namespace ${this.namespace}`);
	}

	createProject(projectName: string): void {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - create new project "${projectName}".`);
		ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} new-project ${projectName} -n ${this.namespace}`);
	}

	deleteProject(projectName: string): void {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - delete "${projectName}".`);
		ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} delete project ${projectName} -n ${this.namespace}`);
	}

	private getPodAndContainerNames(): void {
		KubernetesCommandLineToolsExecutor.pod = this.getWorkspacePodName();
		KubernetesCommandLineToolsExecutor.container = this.getContainerName();
	}

	private isUserLoggedIn(): boolean {
		const whoamiCommandOutput: ShellString = ShellExecutor.execWithLog('oc whoami && oc whoami --show-server=true');

		return (
			whoamiCommandOutput.stdout.includes(OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME) &&
			whoamiCommandOutput.stdout.includes(this.getServerUrl())
		);
	}

	private setNamespace(_namespace: string | undefined): string {
		_namespace =
			_namespace !== undefined
				? _namespace
				: BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL.includes('devspaces')
				? OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME + '-devspaces'
				: BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL.includes('che')
				? OAUTH_CONSTANTS.TS_SELENIUM_OCP_USERNAME + '-che'
				: 'default';
		return _namespace;
	}

	private getServerUrl(): string {
		Logger.debug(`${this.KUBERNETES_COMMAND_LINE_TOOL} - get server api url.`);
		return BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL.replace('devspaces.apps', 'api') + ':6443';
	}
}

// eslint-disable-next-line no-redeclare
export namespace KubernetesCommandLineToolsExecutor {
	export class ContainerTerminal extends KubernetesCommandLineToolsExecutor {
		constructor(cluster: KubernetesCommandLineToolsExecutor) {
			super(cluster.getWorkspaceName, cluster.getNamespace);
		}

		ls(path: string = ''): ShellString {
			return this.executeCommand('ls ' + path);
		}

		pwd(): ShellString {
			return this.executeCommand('pwd');
		}

		cd(path: string): ShellString {
			return this.executeCommand('cd ' + path);
		}

		gitClone(repository: string): ShellString {
			return this.executeCommand('git clone ' + repository);
		}

		removeFolder(path: string): ShellString {
			return this.executeCommand('rm -rf ' + path);
		}

		getEnvValue(envName: string): string {
			envName = envName.replace(/[${}]/g, '');
			const output: ShellString = this.executeCommand(`env | grep ${envName}`);
			return output.stderr ? output.stderr : output.stdout.substring(output.stdout.lastIndexOf('=') + 1).replace('\n', '');
		}
	}
}
