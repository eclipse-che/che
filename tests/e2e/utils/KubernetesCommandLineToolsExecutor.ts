import { echo, exec, ShellString } from 'shelljs';
import { KubernetesCommandLineTool, TestConstants } from '../constants/TestConstants';
import { Logger } from './Logger';
import { ShellExecutor } from './ShellExecutor';
import { TimeoutConstants } from '../constants/TimeoutConstants';

export class KubernetesCommandLineToolsExecutor extends ShellExecutor {
    private static container: string;
    private static pod: string;
    private readonly namespace: string;
    private readonly workspaceName: string | undefined;
    private readonly KUBERNETES_COMMAND_LINE_TOOL: string = TestConstants.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL;

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
            Logger.debug(`${this.getLoggingName(this.loginToOcp.name)}: Login to the "OC" client.`);
            const url: string = this.getServerUrl();
            if (this.isUserLoggedIn()) {
                Logger.debug(`${this.getLoggingName(this.loginToOcp.name)}: User already logged`);
            } else {
                Logger.debug(`${this.getLoggingName(this.loginToOcp.name)}: Login ${url}, ${TestConstants.TS_SELENIUM_OCP_USERNAME}`);
                exec(`oc login --server=${url} -u=${TestConstants.TS_SELENIUM_OCP_USERNAME} -p=${TestConstants.TS_SELENIUM_OCP_PASSWORD} --insecure-skip-tls-verify`);
            }
        } else {
            Logger.debug(`${this.getLoggingName(this.loginToOcp.name)}: doesn't support login command`);
        }
    }

    getContainerName(): string {
        Logger.debug(`${this.getLoggingName(this.getContainerName.name)}: Get container name.`);
        const output: ShellString = this.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} get ${(KubernetesCommandLineToolsExecutor.pod)} -o jsonpath='{.spec.containers[*].name}' -n ${this.namespace}`);
        echo('\n');
        return output.stderr ? output.stderr : output.stdout;
    }

    getWorkspacePodName(): string {
        Logger.debug(`${this.getLoggingName(this.getWorkspacePodName.name)}: Get workspace pod name.`);
        const output: ShellString = this.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} get pod -l controller.devfile.io/devworkspace_name=${this.workspaceName} -n ${this.namespace} -o name`);
        return output.stderr ? output.stderr : output.stdout.replace('\n', '');
    }

    deleteDevWorkspace(): void {
        Logger.debug(`${this.getLoggingName(this.deleteDevWorkspace.name)}: Delete '${this.workspaceName}' workspace`);
        this.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} patch dw ${this.workspaceName} -n ${this.namespace} -p '{ "metadata": { "finalizers": null }}' --type merge || true`);
        this.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} delete dw ${this.workspaceName} -n ${this.namespace} || true`);
        this.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} delete dwt ${TestConstants.TS_SELENIUM_EDITOR}-${this.workspaceName} -n ${this.namespace} || true`);
    }

    applyAndWaitDevWorkspace(yamlConfiguration: string): ShellString {
        if (this.KUBERNETES_COMMAND_LINE_TOOL === KubernetesCommandLineTool.KUBECTL) {
            this.createNamespace();
        }
        this.applyYamlConfigurationAsStringOutput(yamlConfiguration);
        const output: ShellString = this.waitDevWorkspace();
        KubernetesCommandLineToolsExecutor.pod = this.getWorkspacePodName();
        KubernetesCommandLineToolsExecutor.container = this.getContainerName();
        return output;
    }

    executeCommand(commandToExecute: string): ShellString {
        Logger.debug(`${this.getLoggingName(this.executeCommand.name)}:`);
        return this.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} exec -i ${KubernetesCommandLineToolsExecutor.pod} -n ${this.namespace} -c ${KubernetesCommandLineToolsExecutor.container} -- sh -c "${commandToExecute}"`);
    }

    applyYamlConfigurationAsStringOutput(yamlConfiguration: string): void {
        Logger.debug(`${this.getLoggingName(this.applyYamlConfigurationAsStringOutput.name)}:`);
        this.execWithLog(`cat <<EOF | ${this.KUBERNETES_COMMAND_LINE_TOOL} apply -n ${this.namespace} -f - \n` +
            yamlConfiguration + '\n' +
            'EOF');
    }

    waitDevWorkspace(timeout: number = 360): ShellString {
        Logger.debug(`${this.getLoggingName(this.waitDevWorkspace.name)}: Wait till workspace ready.`);
        return this.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} wait -n ${this.namespace} --for=condition=Ready dw ${this.workspaceName} --timeout=${timeout}s`);
    }

    createNamespace(): void {
        Logger.debug(`${this.getLoggingName(this.createNamespace.name)}: Create namespace "${this.namespace}".`);
        this.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} create namespace ${this.namespace}`);
    }

    createProject(projectName: string, timeout: number = TimeoutConstants.TS_SELENIUM_TERMINAL_DEFAULT_TIMEOUT * 2): void {
        Logger.debug(`${this.getLoggingName(this.createProject.name)}: Create new project "${projectName}".`);
        const polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING;
        const attempts: number = Math.ceil(timeout / polling);
        let response: ShellString;
        for (let i: number = 0; i < attempts; i++) {
            response = this.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} new-project ${projectName} -n ${this.namespace}`);
            if ((response.stderr + response.stdout).includes('already exist')) {
                Logger.trace(`${this.getLoggingName(this.createProject.name)} - Project already exist #${(i + 1)}, retrying with ${polling}ms timeout`);
                exec(`sleep ${polling / 1000}s`);
            } else {
                break;
            }
        }
    }

    deleteProject(projectName: string): void {
        Logger.debug(`${this.getLoggingName(this.deleteProject.name)}: Delete "${projectName}".`);
        this.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} delete project ${projectName} -n ${this.namespace} && sleep 5s`);
    }

    private isUserLoggedIn(): boolean {
        const whoamiCommandOutput: ShellString = this.execWithLog('oc whoami && oc whoami --show-server=true');

        return whoamiCommandOutput.stdout.includes(TestConstants.TS_SELENIUM_OCP_USERNAME) && whoamiCommandOutput.stdout.includes(this.getServerUrl());
    }

    private getLoggingName(methodName: string): string {
        return `${this.constructor.name}.${methodName} - ${(this.KUBERNETES_COMMAND_LINE_TOOL)}`;
    }

    private setNamespace(_namespace: string | undefined): string {
        _namespace = _namespace !== undefined ? _namespace
            : TestConstants.TS_SELENIUM_BASE_URL.includes('devspaces') ? TestConstants.TS_SELENIUM_OCP_USERNAME + '-devspaces'
                : TestConstants.TS_SELENIUM_BASE_URL.includes('che') ? TestConstants.TS_SELENIUM_OCP_USERNAME + '-che'
                    : 'default';
        return _namespace;
    }

    private getServerUrl(): string {
        Logger.debug(`${this.getLoggingName(this.getServerUrl.name)}: Get server api url.`);
        return TestConstants.TS_SELENIUM_BASE_URL.replace('devspaces.apps', 'api') + ':6443';
    }
}

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
    }
}
