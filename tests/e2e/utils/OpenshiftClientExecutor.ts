import { echo, exec, ShellString } from 'shelljs';
import { TestConstants } from '../constants/TestConstants';
import { Logger } from './Logger';

export class OpenshiftClientExecutor {
    protected static container: string;
    protected static pod: string | undefined;
    protected workspaceName: string | undefined;
    protected namespace: string;

    constructor(_workspaceName?: string, _namespace?: string) {
        this.workspaceName = _workspaceName;
        this.namespace = this.setNamespace(_namespace);
    }

    get getWorkspaceName(): string {
        return <string>this.workspaceName;
    }

    get getNamespace(): string {
        return this.namespace;
    }

    loginToOcp(): void {
        Logger.debug('OpenshiftClientExecutor.loginToOcp: Login to the \'OC\' client');
        const url: string = this.getServerUrl();
        Logger.debug(url, TestConstants.TS_SELENIUM_OCP_USERNAME);
        exec(`sleep 5
            oc login --server=${url} -u=${TestConstants.TS_SELENIUM_OCP_USERNAME} -p=${TestConstants.TS_SELENIUM_OCP_PASSWORD} --insecure-skip-tls-verify`);
    }

    getContainerName(): string {
        Logger.debug(`OpenshiftClientExecutor.getContainerName: Get container name.`);
        const output: ShellString = this.execWithLog(`oc get ${(OpenshiftClientExecutor.pod)} -o jsonpath='{.spec.containers[*].name}' -n ${this.namespace}`);
        echo('\n');
        return output.stderr ? output.stderr : output.stdout;
    }

    getWorkspacePodName(): string {
        Logger.debug('OpenshiftClientExecutor.getWorkspacePodName: Get workspace pod name.');
        const output: ShellString = this.execWithLog(`oc get pod -l controller.devfile.io/devworkspace_name=${this.workspaceName} -n ${this.namespace} -o name`);
        return output.stderr ? output.stderr : output.stdout.replace('\n', '');
    }

    deleteDevWorkspace(): void {
        Logger.debug(`OpenshiftClientExecutor.deleteWorkspace: Delete '${this.workspaceName}' workspace`);
        this.execWithLog(`oc patch dw ${this.workspaceName} -n ${this.namespace} -p '{ "metadata": { "finalizers": null }}' --type merge || true`);
        this.execWithLog(`oc delete dw ${this.workspaceName} -n ${this.namespace} || true`);
    }

    applyAndWaitDevWorkspace(yamlConfiguration: string): ShellString {
        this.apply(yamlConfiguration);
        const output: ShellString = this.waitDevWorkspace();
        OpenshiftClientExecutor.pod = this.getWorkspacePodName();
        OpenshiftClientExecutor.container = this.getContainerName();
        return output;
    }

    executeCommand(commandToExecute: string): ShellString {
        Logger.debug('OpenshiftClientExecutor.executeCommand');
        return this.execWithLog(`oc exec -i ${OpenshiftClientExecutor.pod} -n ${this.namespace} -c ${OpenshiftClientExecutor.container} -- sh -c "${commandToExecute}"`);
    }

    apply(yamlConfiguration: string): void {
        Logger.debug('OpenshiftClientExecutor.apply:');
        this.execWithLog('cat <<EOF | oc apply -n ' + this.namespace + ' -f - \n' +
            yamlConfiguration + '\n' +
            'EOF');
    }

    waitDevWorkspace(timeout: number = 360): ShellString {
        Logger.debug('OpenshiftClientExecutor.wait: Wait till workspace ready.');
        return this.execWithLog(`oc wait -n ${this.namespace} --for=condition=Ready dw ${this.workspaceName} --timeout=${timeout}s`);
    }

    private setNamespace(_namespace: string | undefined): string {
        return _namespace !== undefined ? _namespace : TestConstants.TS_SELENIUM_OCP_USERNAME + '-devspaces';
    }

    private getServerUrl(): string {
        Logger.debug('OpenshiftClientExecutor.getServerUrl: Get server api url.');
        return TestConstants.TS_SELENIUM_BASE_URL.replace('devspaces.apps', 'api') + ':6443';
    }

    private execWithLog(command: string): ShellString {
        echo(command);
        return exec(command);
    }
}

export namespace OpenshiftClientExecutor {
    export class ContainerTerminal extends OpenshiftClientExecutor {
        constructor(cluster: OpenshiftClientExecutor) {
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
