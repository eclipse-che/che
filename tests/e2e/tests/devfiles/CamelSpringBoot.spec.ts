/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { CLASSES } from '../../inversify.types';
import { LanguageServerTests } from '../../testsLibrary/LanguageServerTests';
import { e2eContainer } from '../../inversify.config';
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../driver/CheReporter';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const commonLanguageServerTests: LanguageServerTests = e2eContainer.get(CLASSES.LanguageServerTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);

const workspaceSampleName: string = 'fuse-rest-http-booster';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/com/redhat/fuse/boosters/rest/http`;
const tabTitle: string = 'CamelRouter.java';
const stack: string = 'Apache Camel based on Spring Boot';
const taskName: string = 'build the project';
let workspaceName: string;

suite(`${stack} test`, async () => {
	suite(`Create ${stack} workspace`, async () => {
		workspaceHandlingTests.createAndOpenWorkspace(stack);

		test('Register running workspace', async () => {
			workspaceName = WorkspaceHandlingTests.getWorkspaceName();
			CheReporter.registerRunningWorkspace(workspaceName);
		});

		projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName);
	});

	suite('Test opening file', async () => {
		// opening file that soon should give time for LS to initialize
		projectAndFileTests.openFile(fileFolderPath, tabTitle);
	});

	suite('Validation of workspace build and run', async () => {
		codeExecutionTests.runTask(taskName, 120_000);
		codeExecutionTests.closeTerminal(taskName);
	});

	suite('Java Language server validation', async () => {
		commonLanguageServerTests.autocomplete(tabTitle, 16, 0, 'from(Endpoint endpoint) : RouteDefinition');
	});

	suite('Camel Language server validation', async () => {
		commonLanguageServerTests.autocomplete(tabTitle, 35, 15, 'timer:timerName');
	});

	suite('Stopping and deleting the workspace', async () => {
		test(`Stop and remowe workspace`, async () => {
			await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
		});
	});
});
