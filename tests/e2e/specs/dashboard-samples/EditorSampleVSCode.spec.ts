import { Logger } from '../../utils/Logger';
import { KubernetesCommandLineToolsExecutor } from '../../utils/KubernetesCommandLineToolsExecutor';
import fs from 'fs';
import path from 'path';
import YAML from 'yaml';
import { e2eContainer } from '../../configs/inversify.config';
import { CLASSES } from '../../configs/inversify.types';
import { LoginTests } from '../../tests-library/LoginTests';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { WorkspaceHandlingTests } from '../../tests-library/WorkspaceHandlingTests';
import { expect } from 'chai';

suite('Check Visual Studio Code (desktop) (SSH) with all samples', function (): void {
	this.timeout(6000000);
	const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
	const pathToSampleFile: string = path.resolve('resources/default-devfile.yaml');
	const workspaceName: string = YAML.parse(fs.readFileSync(pathToSampleFile, 'utf8')).metadata.name;
	const kubernetesCommandLineToolsExecutor: KubernetesCommandLineToolsExecutor = e2eContainer.get(
		CLASSES.KubernetesCommandLineToolsExecutor
	);
	kubernetesCommandLineToolsExecutor.workspaceName = workspaceName;
	const loginTests: LoginTests = e2eContainer.get(CLASSES.LoginTests);
	const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
	const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);

	const vsCodeEditor: string = '//*[@id="editor-selector-card-che-incubator/che-code-sshd/latest"]';
	const titlexPath: string = '/html/body/h1';

	const samplesForCheck: string[] = [
		'Empty Workspace',
		'JBoss EAP 8.0',
		'Java Lombok',
		'Node.js Express',
		'Python',
		'Quarkus REST API',
		'.NET',
		'Ansible',
		'C/C++',
		'Go',
		'PHP'
	];

	const urlsForCheck: string[] = [
		'https://github.com/crw-qe/quarkus-api-example-public/tree/ubi8-latest',
		'https://github.com/crw-qe/ubi9-based-sample-public/tree/ubi9-minimal'
	];

	async function testVSCode(sampleOrUrl: string, isUrl: boolean): Promise<void> {
		Logger.debug(sampleOrUrl);
		await dashboard.openDashboard();
		await workspaceHandlingTests.createAndOpenWorkspaceWithSpecificEditorAndSample(vsCodeEditor, sampleOrUrl, isUrl, titlexPath);

		const headerText: string = await workspaceHandlingTests.getTextFromWorkspaceElement(titlexPath);
		expect('Workspace ' + WorkspaceHandlingTests.getWorkspaceName() + ' is running').equal(headerText);

		await deleteWorkspace();
	}

	async function deleteWorkspace(): Promise<void> {
		await dashboard.openDashboard();
		await browserTabsUtil.closeAllTabsExceptCurrent();
		await dashboard.forceStopWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
		await dashboard.deleteStoppedWorkspaceByUI(WorkspaceHandlingTests.getWorkspaceName());
	}

	suiteSetup('Login into OC', function (): void {
		kubernetesCommandLineToolsExecutor.loginToOcp();
	});

	suiteSetup('Login into Che', async function (): Promise<void> {
		await loginTests.loginIntoChe();
	});

	test('Test VSCode (desktop) (SSH) with default Samples', async function (): Promise<void> {
		for (const sampleName of samplesForCheck) {
			await testVSCode(sampleName, false);
		}
	});

	test('Test VSCode (desktop) (SSH) with ubi', async function (): Promise<void> {
		for (const url of urlsForCheck) {
			await testVSCode(url, true);
		}
	});

	suiteTeardown('Delete default DevWorkspace', function (): void {
		kubernetesCommandLineToolsExecutor.deleteDevWorkspace();
	});
});
