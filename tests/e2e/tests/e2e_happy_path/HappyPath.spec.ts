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
import * as projectAndFileTests from '../../testsLibrary/ProjectAndFileTests';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Logger } from '../../utils/Logger';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const contextMenu: ContextMenu = e2eContainer.get(CLASSES.ContextMenu);
const previewWidget: PreviewWidget = e2eContainer.get(CLASSES.PreviewWidget);
const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
const leftToolbar: LeftToolbar = e2eContainer.get(CLASSES.LeftToolbar);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const debugView: DebugView = e2eContainer.get(CLASSES.DebugView);
const warningDialog: DialogWindow = e2eContainer.get(CLASSES.DialogWindow);
const projectName: string = 'petclinic';
const workspaceRootFolderName: string = 'src';
const workspaceName: string = TestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME;
const pathToJavaFolder: string = `${projectName}/${workspaceRootFolderName}/main/java/org/springframework/samples/petclinic`;
const pathToChangedJavaFileFolder: string = `${projectName}/${workspaceRootFolderName}/main/java/org/springframework/samples/petclinic/system`;
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

suite('Login', async () => {
    test('Login', async () => {
        await driverHelper.navigateToUrl(TestConstants.TS_SELENIUM_BASE_URL);
        await loginPage.login();
    });
});


suite('Validation of workspace start', async () => {
    test('Start workspace', async () => {
        await dashboard.waitPage();
        await dashboard.clickWorkspacesButton();
        await workspaces.waitPage();
        await workspaces.clickOpenButton(workspaceName);
    });

    await projectAndFileTests.waitWorkspaceReadiness(projectName, workspaceRootFolderName);
});

suite('Language server validation', async () => {
    test('Java LS initialization', async () => {
        await projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        await editor.selectTab(javaFileName);

        try {
            await ide.checkLsInitializationStart('Activating Language Support for Java');
            await ide.waitStatusBarTextAbsence('Activating Language Support for Java', 900_000);
        } catch (err) {
            if (!(err instanceof error.TimeoutError)) {
                throw err;
            }

            console.log('Known flakiness has occurred https://github.com/eclipse/che/issues/17864');
            await ide.waitStatusBarContains('Activating Java Test Runner');
            await ide.waitStatusBarTextAbsence('Activating Java Test Runner', 900_000);
        }

        await checkJavaPathCompletion();
    });

    test('Autocomplete', async () => {
        await editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        await editor.pressControlSpaceCombination(javaFileName);
        await editor.waitSuggestionContainer();
        await editor.waitSuggestion(javaFileName, 'SpringApplication - org.springframework.boot');
    });

    test('Error highlighting', async () => {
        const textForErrorDisplaying: string = '$#%@#';

        await editor.type(javaFileName, textForErrorDisplaying, 30);

        try {
            await editor.waitErrorInLine(30, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT * 3);
        } catch (err) {
            Logger.debug('Workaround for the https://github.com/eclipse/che/issues/18974 issue.');
            await (await driverHelper.getDriver()).navigate().refresh();

            await ide.waitAndSwitchToIdeFrame();
            await editor.selectTab(javaFileName);
            await editor.waitErrorInLine(30, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT * 3);
        }

        await editor.performKeyCombination(javaFileName, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
        await editor.waitErrorInLineDisappearance(30);
    });

    test('Suggestion', async () => {
        await editor.moveCursorToLineAndChar(javaFileName, 32, 21);
        await editor.pressControlSpaceCombination(javaFileName);
        await editor.waitSuggestionWithScrolling(javaFileName, 'run(Class<?> primarySource, String... args) : ConfigurableApplicationContext', 120_000);
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
        let buildTaskName: string = 'build-file-output';
        await topMenu.runTask('build-file-output');

        // workaround for issue: https://github.com/eclipse/che/issues/14771

        // await projectTree.expandPathAndOpenFileInAssociatedWorkspace(projectName, 'build-output.txt');
        await terminal.waitIconSuccess(buildTaskName, 250_000);
    });

    test('Run application', async () => {
        await topMenu.runTask('run');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120_000);
        applicationUrl = await ide.getApplicationUrlFromNotification('Redirect is now enabled on port 8080', 120_000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120_000);
    });

    test('Check the running application', async () => {
        await previewWidget.waitApplicationOpened(applicationUrl, 60_000);
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60_000, 10_000);
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
        let buildTaskName: string = 'build';

        await topMenu.runTask('build');
        await terminal.waitIconSuccess(buildTaskName, 250_000);
    });

    test('Run application with changes', async () => {
        await topMenu.runTask('run-with-changes');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120_000);
        applicationUrl = await ide.getApplicationUrlFromNotification('Redirect is now enabled on port 8080', 120_000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120_000);
    });

    test('Check changes are displayed', async () => {
        await previewWidget.waitApplicationOpened(applicationUrl, 60_000);
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60_000, 10_000);
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

    test('Launch debug', async () => {
        await topMenu.runTask('run-debug');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 180_000);
        applicationUrl = await ide.getApplicationUrlFromNotification('Redirect is now enabled on port 8080', 180_000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 180_000);
    });

    test('Check content of the launched application', async () => {
        await previewWidget.waitApplicationOpened(applicationUrl, 60_000);
        await previewWidget.waitAndSwitchToWidgetFrame();
        await previewWidget.waitAndClick(SpringAppLocators.springHomeButtonLocator);
        await driverHelper.getDriver().switchTo().defaultContent();
        await ide.waitAndSwitchToIdeFrame();
    });


    test('Run debug and check application stop in the breakpoint', async () => {
        await projectTree.expandPathAndOpenFile(pathToJavaFolder + '/system', weclomeControllerJavaFileName);
        await editor.selectTab(weclomeControllerJavaFileName);
        await topMenu.selectOption('View', 'Debug');
        await ide.waitLeftToolbarButton(LeftToolbarButton.Debug);
        await debugView.clickOnDebugConfigurationDropDown();
        await debugView.clickOnDebugConfigurationItem('Debug (Attach) - Remote');
        await debugView.clickOnRunDebugButton();

        try {
            await debugView.waitForDebuggerToConnect();
        } catch (err) {
            Logger.debug('Workaround for the https://github.com/eclipse/che/issues/18034 issue.');
            await debugView.clickOnThreadsViewTitle();
            
            await debugView.waitForDebuggerToConnect();
        }

        await editor.selectTab(weclomeControllerJavaFileName);
        await editor.activateBreakpoint(weclomeControllerJavaFileName, 27);

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
        await previewWidget.waitVisibility(SpringAppLocators.springErrorMessageLocator, 15_000);
    } catch (err) {

        await driverHelper.getDriver().switchTo().defaultContent();
        await ide.waitAndSwitchToIdeFrame();

        await previewWidget.waitAndSwitchToWidgetFrame();
        await previewWidget.waitVisibility(SpringAppLocators.springErrorMessageLocator, 15_000);
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
