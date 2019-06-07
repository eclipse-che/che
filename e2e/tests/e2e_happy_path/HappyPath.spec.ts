/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../../inversify.config';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { Editor } from '../../pageobjects/ide/Editor';
import { PreviewWidget } from '../../pageobjects/ide/PreviewWidget';
import { GitHubPlugin } from '../../pageobjects/ide/GitHubPlugin';
import { TestConstants } from '../../TestConstants';
import { RightToolbar } from '../../pageobjects/ide/RightToolbar';
import { By } from 'selenium-webdriver';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const previewWidget: PreviewWidget = e2eContainer.get(CLASSES.PreviewWidget);
const githubPlugin: GitHubPlugin = e2eContainer.get(CLASSES.GitHubPlugin);
const rightToolbar: RightToolbar = e2eContainer.get(CLASSES.RightToolbar);

const projectName: string = 'petclinic';
const namespace: string = TestConstants.TS_SELENIUM_USERNAME;
const workspaceName: string = TestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME;
const workspaceUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${namespace}/${workspaceName}`;
const pathToJavaFolder: string = `${projectName}/src/main/java/org/springframework/samples/petclinic`;
const javaFileName: string = 'PetClinicApplication.java';
const pathToYamlFolder: string = projectName;
const yamlFileName: string = 'devfile.yaml';
const expectedGithubChanges: string = '_remote.repositories %3F/.m2/repository/antlr/antlr/2.7.7\n' + 'U';
const springTitleLocator: By = By.xpath('//div[@class=\'container-fluid\']//h2[text()=\'Welcome\']');


suite('Ide checks', async () => {
    test('Build application', async () => {
        await driverHelper.navigateTo(workspaceUrl);
        await ide.waitWorkspaceAndIde(namespace, workspaceName);
        await projectTree.openProjectTreeContainer();
        await projectTree.waitProjectImported(projectName, 'src');
        await projectTree.expandItem(`/${projectName}`);
        await topMenu.waitTopMenu();
        await ide.closeAllNotifications();
        await topMenu.clickOnTopMenuButton('Terminal');
        await topMenu.clickOnSubmenuItem('Run Task...');
        await quickOpenContainer.clickOnContainerItem('che: build-file-output');

        await projectTree.expandPathAndOpenFile(projectName, 'build-output.txt');
        await editor.waitEditorAvailable('build-output.txt');
        await editor.clickOnTab('build-output.txt');
        await editor.waitTabFocused('build-output.txt');
        await editor.followAndWaitForText('build-output.txt', '[INFO] BUILD SUCCESS', 180000, 5000);
    });

    test('Run application', async () => {
        await topMenu.waitTopMenu();
        await ide.closeAllNotifications();
        await topMenu.clickOnTopMenuButton('Terminal');
        await topMenu.clickOnSubmenuItem('Run Task...');
        await quickOpenContainer.clickOnContainerItem('che: run');

        await ide.waitNotification('A new process is now listening on port 8080', 120000);
        await ide.clickOnNotificationButton('A new process is now listening on port 8080', 'yes');

        await ide.waitNotification('Redirect is now enabled on port 8080', 120000);
        await ide.clickOnNotificationButton('Redirect is now enabled on port 8080', 'Open Link');
        await previewWidget.waitContentAvailable(springTitleLocator, 60000, 10000);
        await rightToolbar.clickOnToolIcon('Preview');
        await previewWidget.waitPreviewWidgetAbsence();
    });

    test('Java LS initialization', async () => {
        await projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        await editor.waitEditorAvailable(javaFileName);
        await editor.clickOnTab(javaFileName);
        await editor.waitTabFocused(javaFileName);
        await ide.waitStatusBarTextAbcence('Starting Java Language Server', 360000);
        await editor.moveCursorToLineAndChar(javaFileName, 32, 27);
        await editor.pressControlSpaceCombination(javaFileName);
        await editor.waitSuggestion(javaFileName, 'run(Class<?> primarySource, String... args) : ConfigurableApplicationContext', 40000);
    });

    test.skip('Yaml LS initialization', async () => {
        await projectTree.expandPathAndOpenFile(pathToYamlFolder, yamlFileName);
        await editor.waitEditorAvailable(yamlFileName);
        await editor.clickOnTab(yamlFileName);
        await editor.waitTabFocused(yamlFileName);
        await ide.waitStatusBarContains('Starting Yaml Language Server');
        await ide.waitStatusBarContains('100% Starting Yaml Language Server');
        await ide.waitStatusBarTextAbcence('Starting Yaml Language Server');
    });

    test.skip('Github plugin initialization', async () => {
        await githubPlugin.openGitHubPluginContainer();
        await githubPlugin.waitChangesPresence(expectedGithubChanges);
    });
});
