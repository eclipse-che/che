/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { Key } from 'selenium-webdriver';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Editor } from '../../pageobjects/ide/Editor';
import { DriverHelper } from '../../utils/DriverHelper';
import { PreferencesHandler } from '../../utils/PreferencesHandler';
import { TestConstants } from '../../TestConstants';
import { TimeoutConstants } from '../../TimeoutConstants';
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import { Logger } from '../../utils/Logger';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const preferencesHandler: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);

let workspaceName: string = '';

const devfileUrl: string = 'https://raw.githubusercontent.com/eclipse/che/master/tests/e2e/files/devfiles/plugins/VscodeYamlPlugin.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'nodejs-web-app';
const pathToFile: string = `${projectName}`;
const yamlFileName: string = 'routes.yaml';
const yamlSchema = { 'https://raw.githubusercontent.com/apache/camel-k-runtime/camel-k-runtime-parent-1.5.0/camel-k-loader-yaml/camel-k-loader-yaml/src/generated/resources/camel-yaml-dsl.json': '*.yaml' };

suite('The "VscodeYamlPlugin" userstory', async () => {
    suite('Create workspace', async () => {
        test('Set yaml schema', async () => {
            await preferencesHandler.setPreference('yaml.schemas', yamlSchema);
        });

        test('Create workspace using factory', async () => {
            await driverHelper.navigateToUrl(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);

            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });
    });

    suite('Check workspace readiness to work', async () => {
        test('Wait until project is imported', async () => {
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(projectName, 'app');
        });
    });

    suite('Check the "vscode-yaml" plugin', async () => {
        test('Check autocomplete', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);
            await editor.waitSuggestion(yamlFileName, 'from', 60000, 18, 5);
        });

        test('Check error appearance', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);

            await editor.type(yamlFileName, Key.SPACE, 19);
            await editor.waitErrorInLine(19);
        });

        test('Check error disappearance', async () => {
            await editor.performKeyCombination(yamlFileName, Key.BACK_SPACE);
            await editor.waitErrorInLineDisappearance(19);
        });

        test('To unformat the "yaml" file', async () => {
            const expectedTextBeforeFormating: string = 'uri:     "timer:tick"';
            await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);

            await editor.selectTab(yamlFileName);
            await editor.moveCursorToLineAndChar(yamlFileName, 19, 10);
            await editor.performKeyCombination(yamlFileName, Key.chord(Key.SPACE, Key.SPACE, Key.SPACE, Key.SPACE));
            await editor.waitText(yamlFileName, expectedTextBeforeFormating, 60000, 10000);
        });

        test('To format the "yaml" document', async () => {
            const expectedTextAfterFormating: string = 'uri: "timer:tick"';

            await editor.type(yamlFileName, Key.chord(Key.CONTROL, Key.SHIFT, 'I'), 19);
            await editor.waitText(yamlFileName, expectedTextAfterFormating, 60000, 10000);
        });

    });

    suite('Delete workspace', async () => {
        test('Stop and remove workspace', async () => {
            if (TestConstants.TS_DELETE_PLUGINS_TEST_WORKSPACE === 'true') {
                await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });
    });
});
