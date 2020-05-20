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
import { TYPES, CLASSES } from '../../inversify.types';
import { Ide, LeftToolbarButton } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { Editor } from '../../pageobjects/ide/Editor';
import { PreviewWidget } from '../../pageobjects/ide/PreviewWidget';
import { TestConstants } from '../../TestConstants';
import { LeftToolbar } from '../../pageobjects/ide/LeftToolBar';
import { By, Key, error } from 'selenium-webdriver';
import { DebugView } from '../../pageobjects/ide/DebugView';
import { DialogWindow } from '../../pageobjects/ide/DialogWindow';
import { Terminal } from '../../pageobjects/ide/Terminal';
import { ICheLoginPage } from '../../pageobjects/login/ICheLoginPage';
import * as fs from 'fs';
import { ContextMenu } from '../../pageobjects/ide/ContextMenu';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const contextMenu: ContextMenu = e2eContainer.get(CLASSES.ContextMenu);
const previewWidget: PreviewWidget = e2eContainer.get(CLASSES.PreviewWidget);
const leftToolbar: LeftToolbar = e2eContainer.get(CLASSES.LeftToolbar);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const debugView: DebugView = e2eContainer.get(CLASSES.DebugView);
const warningDialog: DialogWindow = e2eContainer.get(CLASSES.DialogWindow);
const projectName: string = 'petclinic';
const namespace: string = TestConstants.TS_SELENIUM_USERNAME;
const workspaceName: string = TestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME;
const workspaceUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${namespace}/${workspaceName}`;
const pathToJavaFolder: string = `${projectName}/src/main/java/org/springframework/samples/petclinic`;
const pathToChangedJavaFileFolder: string = `${projectName}/src/main/java/org/springframework/samples/petclinic/system`;
const classPathFilename: string = '.classpath';
const javaFileName: string = 'PetClinicApplication.java';
const weclomeControllerJavaFileName: string = 'WelcomeController.java';
const changedJavaFileName: string = 'CrashController.java';
const textForErrorMessageChange: string = 'HHHHHHHHHHHHH';
const codeNavigationClassName: string = 'SpringApplication.class';
const pathToYamlFolder: string = projectName;
const yamlFileName: string = 'devfile.yaml';
const loginPage: ICheLoginPage = e2eContainer.get<ICheLoginPage>(TYPES.CheLogin);

const SpringAppLocators = {
    springTitleLocator: By.xpath('//div[@class=\'container-fluid\']//h2[text()=\'Welcome\']'),
    springMenuButtonLocator: By.css('button[data-target=\'#main-navbar\']'),
    springErrorButtonLocator: By.xpath('//div[@id=\'main-navbar\']//span[text()=\'Error\']'),
    springHomeButtonLocator: By.className('navbar-brand'),
    springErrorMessageLocator: By.xpath(`//h2[text()='Something happened...']`)
};


suite('Validation of workspace start', async () => {
    test('Open workspace', async () => {
        await driverHelper.navigateToUrl(workspaceUrl);
        await loginPage.login();
    });

    test('Wait workspace running state', async () => {
        await ide.waitWorkspaceAndIde();
    });

    test('Wait until project is imported', async () => {
        await projectTree.openProjectTreeContainer();
        await projectTree.waitProjectImported(projectName, 'src');
    });

});

suite('Language server validation', async () => {
    test('Java LS initialization', async () => {
        await projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        await editor.selectTab(javaFileName);

        try {
            await ide.checkLsInitializationStart('Activating Language Support for Java');
        } catch (err) {
            if (!(err instanceof error.TimeoutError)) {
                throw err;
            }

            console.log('Known flakiness has occurred https://github.com/eclipse/che/issues/14944');
            await driverHelper.reloadPage();
            await ide.waitStatusBarContains('Activating Language Support for Java');
        }

        await ide.waitStatusBarTextAbsence('Activating Language Support for Java', 1800000);
        await checkJavaPathCompletion();
    });

    test('Error highlighting', async () => {
        await editor.type(javaFileName, 'error', 30);
        await editor.waitErrorInLine(30);
        await editor.performKeyCombination(javaFileName, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
        await editor.waitErrorInLineDisappearance(30);
    });

    test('Autocomplete', async () => {
        await editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        await editor.pressControlSpaceCombination(javaFileName);
        await editor.waitSuggestionContainer();
        await editor.waitSuggestion(javaFileName, 'SpringApplication - org.springframework.boot');
    });

    test('Suggestion', async () => {
        await editor.moveCursorToLineAndChar(javaFileName, 32, 21);
        await editor.pressControlSpaceCombination(javaFileName);
        await editor.waitSuggestionWithScrolling(javaFileName, 'run(Class<?> primarySource, String... args) : ConfigurableApplicationContext', 120000);
    });

    test('Codenavigation', async () => {
        await editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        try {
            await editor.performKeyCombination(javaFileName, Key.chord(Key.CONTROL, Key.F12));
            await editor.waitEditorAvailable(codeNavigationClassName);
        } catch (err) {
            // workaround for issue: https://github.com/eclipse/che/issues/14520
            if (err instanceof error.TimeoutError) {
                checkCodeNavigationWithContextMenu();
            }
        }
    });

    test.skip('Yaml LS initialization', async () => {
        await projectTree.expandPathAndOpenFile(pathToYamlFolder, yamlFileName);
        await editor.waitEditorAvailable(yamlFileName);
        await editor.clickOnTab(yamlFileName);
        await editor.waitTabFocused(yamlFileName);
        await ide.waitStatusBarContains('Starting Yaml Language Server');
        await ide.waitStatusBarContains('100% Starting Yaml Language Server');
        await ide.waitStatusBarTextAbsence('Starting Yaml Language Server');
    });
});

suite('Validation of workspace build and run', async () => {
    let applicationUrl: string = '';

    test('Build application', async () => {
        await topMenu.runTask('build-file-output');

        // workaround for issue: https://github.com/eclipse/che/issues/14771

        // await projectTree.expandPathAndOpenFileInAssociatedWorkspace(projectName, 'build-output.txt');
        await projectTree.expandPathAndOpenFile(projectName, 'result-build-output.txt', 220000);
        await editor.waitText('result-build-output.txt', '[INFO] BUILD SUCCESS');
        // await editor.followAndWaitForText('build-output.txt', '[INFO] BUILD SUCCESS', 300000, 10000);
    });

    test('Run application', async () => {
        await topMenu.runTask('run');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        applicationUrl = await ide.getApplicationUrlFromNotification('Redirect is now enabled on port 8080', 120000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
    });

    test('Check the running application', async () => {
        await previewWidget.waitApplicationOpened(applicationUrl, 60000);
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60000, 10000);
    });

    test('Close preview widget', async () => {
        await leftToolbar.clickOnToolIcon('Preview');
        await previewWidget.waitPreviewWidgetAbsence();

    });

    test('Close the terminal running tasks', async () => {
        await terminal.closeTerminalTab('build-file-output');
        await terminal.rejectTerminalProcess('run');
        await terminal.closeTerminalTab('run');
        await warningDialog.waitAndCloseIfAppear();
    });
});

suite('Display source code changes in the running application', async () => {
    let applicationUrl: string = '';

    test('Change source code', async () => {
        await projectTree.expandPathAndOpenFile(pathToChangedJavaFileFolder, changedJavaFileName);
        await editor.waitEditorAvailable(changedJavaFileName);
        await editor.clickOnTab(changedJavaFileName);
        await editor.waitTabFocused(changedJavaFileName);

        await editor.moveCursorToLineAndChar(changedJavaFileName, 34, 89);
        await editor.performKeyCombination(changedJavaFileName, textForErrorMessageChange);
        await editor.performKeyCombination(changedJavaFileName, Key.chord(Key.CONTROL, 's'));
    });

    test('Build application with changes', async () => {
        await topMenu.runTask('build');
        await projectTree.collapseProjectTree(projectName + '/src', 'main');
        await projectTree.expandPathAndOpenFile(projectName, 'result-build.txt', 300000);
        await editor.waitText('result-build.txt', '[INFO] BUILD SUCCESS');

        // workaround for issue: https://github.com/eclipse/che/issues/14771

        /*await projectTree.expandPathAndOpenFileInAssociatedWorkspace(projectName, 'build.txt');
        await editor.waitEditorAvailable('build.txt');
        await editor.clickOnTab('build.txt');
        await editor.waitTabFocused('build.txt');
        await editor.followAndWaitForText('build.txt', '[INFO] BUILD SUCCESS', 300000, 5000);*/


    });

    test('Run application with changes', async () => {
        await topMenu.runTask('run-with-changes');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        applicationUrl = await ide.getApplicationUrlFromNotification('Redirect is now enabled on port 8080', 120000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
    });

    test('Check changes are displayed', async () => {
        await previewWidget.waitApplicationOpened(applicationUrl, 60000);
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60000, 10000);
        await checkErrorMessageInApplicationController();
    });

    test('Close preview widget', async () => {
        await leftToolbar.clickOnToolIcon('Preview');
        await previewWidget.waitPreviewWidgetAbsence();
    });

    test('Close running terminal processes and tabs', async () => {
        await terminal.rejectTerminalProcess('run-with-changes');
        await terminal.closeTerminalTab('run-with-changes');
        await warningDialog.waitAndCloseIfAppear();
    });
});

suite('Validation of debug functionality', async () => {
    let applicationUrl: string = '';

    test('Open file and activate breakpoint', async () => {
        await projectTree.expandPathAndOpenFile(pathToJavaFolder + '/system', weclomeControllerJavaFileName);
        await editor.activateBreakpoint(weclomeControllerJavaFileName, 27);
    });

    test('Launch debug', async () => {
        await topMenu.runTask('run-debug');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 180000);
        applicationUrl = await ide.getApplicationUrlFromNotification('Redirect is now enabled on port 8080', 180000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 180000);
    });

    test('Check content of the launched application', async () => {
        await previewWidget.waitApplicationOpened(applicationUrl, 60000);
        await previewWidget.waitAndSwitchToWidgetFrame();
        await previewWidget.waitAndClick(SpringAppLocators.springHomeButtonLocator);
        await driverHelper.getDriver().switchTo().defaultContent();
        await ide.waitAndSwitchToIdeFrame();
    });


    test('Run debug and check application stop in the breakpoint', async () => {
        await editor.selectTab(weclomeControllerJavaFileName);
        await topMenu.selectOption('View', 'Debug');
        await ide.waitLeftToolbarButton(LeftToolbarButton.Debug);
        await debugView.clickOnDebugConfigurationDropDown();
        await debugView.clickOnDebugConfigurationItem('Debug (Attach) - Remote');
        await debugView.clickOnRunDebugButton();
        await debugView.waitForDebuggerToConnect();
        await previewWidget.refreshPage();
        try {
            await editor.waitStoppedDebugBreakpoint(weclomeControllerJavaFileName, 27);
        } catch (err) {
            await previewWidget.refreshPage();
            await editor.waitStoppedDebugBreakpoint(weclomeControllerJavaFileName, 27);
        }
    });
});

async function checkErrorMessageInApplicationController() {
    await previewWidget.waitAndSwitchToWidgetFrame();
    await previewWidget.waitAndClick(SpringAppLocators.springMenuButtonLocator);
    await previewWidget.waitAndClick(SpringAppLocators.springErrorButtonLocator);

    try {
        await previewWidget.waitVisibility(SpringAppLocators.springErrorMessageLocator);
    } catch (err) {

        await driverHelper.getDriver().switchTo().defaultContent();
        await ide.waitAndSwitchToIdeFrame();

        await previewWidget.waitAndSwitchToWidgetFrame();
        await previewWidget.waitVisibility(SpringAppLocators.springErrorMessageLocator);
    }


    await driverHelper.getDriver().switchTo().defaultContent();
    await ide.waitAndSwitchToIdeFrame();
}

async function checkCodeNavigationWithContextMenu() {
    await contextMenu.invokeContextMenuOnActiveElementWithKeys();
    await contextMenu.waitContextMenuAndClickOnItem('Go to Definition');
    console.log('Known isuue https://github.com/eclipse/che/issues/14520.');
}

async function checkJavaPathCompletion() {
    if (await ide.isNotificationPresent('Classpath is incomplete. Only syntax errors will be reported')) {
        const classpathText: string = fs.readFileSync('./files/happy-path/petclinic-classpath.txt', 'utf8');
        const workaroundReportText: string = '\n############################## \n\n' +
            'Known issue: https://github.com/eclipse/che/issues/13427 \n' +
            '\"Java LS \"Classpath is incomplete\" warning when loading petclinic\" \n' +
            '\".classpath\" will be configured with next settings: \n\n' +
            classpathText + '\n' +
            '############################## \n';

        console.log(workaroundReportText);

        await projectTree.expandPathAndOpenFile(projectName, classPathFilename);
        await editor.waitEditorAvailable(classPathFilename);
        await editor.type(classPathFilename, Key.chord(Key.CONTROL, 'a'), 1);
        await editor.performKeyCombination(classPathFilename, Key.DELETE);
        await editor.type(classPathFilename, classpathText, 1);
        await editor.waitTabWithSavedStatus(classPathFilename);
    }
}

