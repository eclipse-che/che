/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../../inversify.config';
import { CLASSES } from '../../../inversify.types';
import { ProjectTree } from '../../../pageobjects/ide/theia/ProjectTree';
import { Editor } from '../../../pageobjects/ide/theia/Editor';
import { TestConstants } from '../../../TestConstants';
import { WorkspaceHandlingTests } from '../../../testsLibrary/WorkspaceHandlingTests';
import { BrowserTabsUtil } from '../../../utils/BrowserTabsUtil';
import { PreferencesHandlerTheia } from '../../../utils/theia/PreferencesHandlerTheia';
import { ProjectAndFileTestsTheia } from '../../../testsLibrary/theia/ProjectAndFileTestsTheia';
import { Key } from 'selenium-webdriver';

const projectAndFileTests: ProjectAndFileTestsTheia = e2eContainer.get(CLASSES.ProjectAndFileTestsTheia);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);

const devfileUrl: string = TestConstants.TS_TEST_WORKSPACE_DEVFILE_REPO || 'https://github.com/che-samples/web-nodejs-sample/tree/yaml-plugin';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'web-nodejs-sample';
const subRootFolder: string = 'app';
const pathToFile: string = `${projectName}`;
const yamlFileName: string = 'routes.yaml';
const yamlSchema = { 'yaml-schema.json': '*.yaml' };

suite('The "VscodeYamlPlugin" userstory', async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();

        projectAndFileTests.waitWorkspaceReadiness(projectName, subRootFolder);
    });

    suite('Check the "vscode-yaml" plugin', async () => {
        test('Set confirmExit preference to never', async () => {
            await preferencesHandler.setPreferenceUsingUI('application.confirmExit', 'never');
        });

        test('Set the yaml schema path', async () => {
            await preferencesHandler.setPreferenceUsingUI('yaml.schemas', yamlSchema);
        });

        test('Check autocomplete', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);
            await editor.waitSuggestion(yamlFileName, 'from', 60000, 18, 5);
        });

        test('Check error appearance', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);

            await editor.type(yamlFileName, Key.SPACE, 20);
            await editor.waitErrorInLine(20, yamlFileName);
        });

        test('Check error disappearance', async () => {
            await editor.performKeyCombination(yamlFileName, Key.BACK_SPACE);
            await editor.waitErrorInLineDisappearance(20, yamlFileName);
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

    suite ('Stopping and deleting the workspace', async () => {
        test('Stop and remove workspace', async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });

});
