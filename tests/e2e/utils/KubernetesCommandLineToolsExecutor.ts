import { echo, exec, ShellString } from 'shelljs';
import { KubernetesCommandLineTool, TestConstants } from '../constants/TestConstants';
import { Logger } from './Logger';
import { ShellExecutor } from './ShellExecutor';
import * as path from 'path';

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
        const output: ShellString = ShellExecutor.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} get ${(KubernetesCommandLineToolsExecutor.pod)} -o jsonpath='{.spec.containers[*].name}' -n ${this.namespace}`);
        echo('\n');
        return output.stderr ? output.stderr : output.stdout;
    }

    getWorkspacePodName(): string {
        Logger.debug(`${this.getLoggingName(this.getWorkspacePodName.name)}: Get workspace pod name.`);
        const output: ShellString = ShellExecutor.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} get pod -l controller.devfile.io/devworkspace_name=${this.workspaceName} -n ${this.namespace} -o name`);
        return output.stderr ? output.stderr : output.stdout.replace('\n', '');
    }

    deleteDevWorkspace(): void {
        Logger.debug(`${this.getLoggingName(this.deleteDevWorkspace.name)}: Delete '${this.workspaceName}' workspace`);
        ShellExecutor.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} patch dw ${this.workspaceName} -n ${this.namespace} -p '{ "metadata": { "finalizers": null }}' --type merge || true`);
        ShellExecutor.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} delete dw ${this.workspaceName} -n ${this.namespace} || true`);
        ShellExecutor.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} delete dwt ${TestConstants.TS_SELENIUM_EDITOR}-${this.workspaceName} -n ${this.namespace} || true`);
    }

    applyAndWaitDevWorkspace(yamlConfiguration: string): ShellString {
        if (this.KUBERNETES_COMMAND_LINE_TOOL === KubernetesCommandLineTool.KUBECTL) {
            this.createNamespace();
        }
        this.applyYamlConfigurationAsStringOutput(yamlConfiguration);
        return this.waitDevWorkspace();
    }

    executeCommand(commandToExecute: string, container: string = KubernetesCommandLineToolsExecutor.container): ShellString {
        Logger.debug(`${this.getLoggingName(this.executeCommand.name)}:`);
        return ShellExecutor.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} exec -i ${KubernetesCommandLineToolsExecutor.pod} -n ${this.namespace} -c ${container} -- sh -c '${commandToExecute}'`);
    }

    applyYamlConfigurationAsStringOutput(yamlConfiguration: string): ShellString {
        Logger.debug(`${this.getLoggingName(this.applyYamlConfigurationAsStringOutput.name)}:`);
        return ShellExecutor.execWithLog(`cat <<EOF | ${this.KUBERNETES_COMMAND_LINE_TOOL} apply -n ${this.namespace} -f - \n` +
            yamlConfiguration + '\n' +
            'EOF');
    }

    applyYamlConfigurationAsFile(pathToFile: string): void {
        Logger.debug(`${this.getLoggingName(this.applyYamlConfigurationAsFile.name)}:`);
        ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} apply -n ${this.namespace} -f "${path.resolve(pathToFile)}"`);
    }

    getDevWorkspaceYamlConfiguration(): ShellString {
        Logger.debug(`${this.getLoggingName(this.getDevWorkspaceYamlConfiguration.name)}:`);
        return ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} get dw ${this.workspaceName} -n ${this.namespace} -o yaml`);
    }

    waitDevWorkspace(timeout: number = 360): ShellString {
        Logger.debug(`${this.getLoggingName(this.waitDevWorkspace.name)}: Wait till workspace ready.`);
        const output: ShellString = ShellExecutor.execWithLog(`${(this.KUBERNETES_COMMAND_LINE_TOOL)} wait -n ${this.namespace} --for=condition=Ready dw ${this.workspaceName} --timeout=${timeout}s`);
        this.getPodAndContainerNames();
        return output;
    }

    createNamespace(): void {
        Logger.debug(`${this.getLoggingName(this.createNamespace.name)}: Create namespace "${this.namespace}".`);
        ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} create namespace ${this.namespace}`);
    }

    createProject(projectName: string): void {
        Logger.debug(`${this.getLoggingName(this.createProject.name)}: Create new project "${projectName}".`);
        ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} new-project ${projectName} -n ${this.namespace}`);
    }

    deleteProject(projectName: string): void {
        Logger.debug(`${this.getLoggingName(this.deleteProject.name)}: Delete "${projectName}".`);
        ShellExecutor.execWithLog(`${this.KUBERNETES_COMMAND_LINE_TOOL} delete project ${projectName} -n ${this.namespace}`);
    }

    private getPodAndContainerNames(): void {
        KubernetesCommandLineToolsExecutor.pod = this.getWorkspacePodName();
        KubernetesCommandLineToolsExecutor.container = this.getContainerName();
    }

    private isUserLoggedIn(): boolean {
        const whoamiCommandOutput: ShellString = ShellExecutor.execWithLog('oc whoami && oc whoami --show-server=true');

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

        getEnvValue(envName: string): string {
            envName = envName.replace(/[${}]/g, '');
            const output: ShellString = this.executeCommand(`env | grep ${envName}`);
            return output.stderr ? output.stderr :
                output.stdout
                    .substring(output.stdout.lastIndexOf('=') + 1)
                    .replace('\n', '');
        }
    }
}
