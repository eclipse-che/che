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
import { Ide, LeftToolbarButton } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { Editor } from '../../pageobjects/ide/Editor';
import { PreviewWidget } from '../../pageobjects/ide/PreviewWidget';
import { TestConstants } from '../../TestConstants';
import { By, Key, error } from 'selenium-webdriver';
import { DebugView } from '../../pageobjects/ide/DebugView';
import { DialogWindow } from '../../pageobjects/ide/DialogWindow';
import { Terminal } from '../../pageobjects/ide/Terminal';
import * as fs from 'fs';
import { Workspaces } from '../../pageobjects/dashboard/Workspaces';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Logger } from '../../utils/Logger';
import { RightToolBar } from '../../pageobjects/ide/RightToolBar';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import CheReporter from '../../driver/CheReporter';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const previewWidget: PreviewWidget = e2eContainer.get(CLASSES.PreviewWidget);
const workspaces: Workspaces = e2eContainer.get(CLASSES.Workspaces);
const rightToolBar: RightToolBar = e2eContainer.get(CLASSES.RightToolBar);
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
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const globalTaskScope = 'Global';

const SpringAppLocators = {
    springTitleLocator: By.xpath('//div[@class=\'container-fluid\']//h2[text()=\'Welcome\']'),
    springMenuButtonLocator: By.css('button[data-target=\'#main-navbar\']'),
    springErrorButtonLocator: By.xpath('//div[@id=\'main-navbar\']//span[text()=\'Error\']'),
    springHomeButtonLocator: By.className('navbar-brand'),
    springErrorMessageLocator: By.xpath(`//h2[text()='Something happened...']`),
};

suite('Validation of workspace start', async () => {
    test('Start workspace', async () => {
        await dashboard.waitPage();
        await dashboard.clickWorkspacesButton();
        await workspaces.waitPage();
        await workspaces.clickOpenButton(workspaceName);
    });

    test('Register running workspace', async () => {
        CheReporter.registerRunningWorkspace(workspaceName);
    });

    test('Wait for workspace readiness', async () => {
        await ide.waitAndSwitchToIdeFrame();
        await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
        await projectTree.openProjectTreeContainer();
        await ide.waitNotificationAndClickOnButton('Do you trust the authors of', 'Yes, I trust', 60_000);
        await projectTree.waitProjectImported(projectName, workspaceRootFolderName);
    });
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
        await driverHelper.getDriver().sleep(TimeoutConstants.TS_SUGGESTION_TIMEOUT);   // workaround https://github.com/eclipse/che/issues/19004

        const textForErrorDisplaying: string = '$';
        await editor.type(javaFileName, textForErrorDisplaying, 30);

        try {
            await editor.waitErrorInLine(30, javaFileName, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT);
        } catch (err) {
            Logger.debug('Workaround for the https://github.com/eclipse/che/issues/18974.');
            await browserTabsUtil.refreshPage();
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde();
            await editor.waitErrorInLine(30, javaFileName, TimeoutConstants.TS_ERROR_HIGHLIGHTING_TIMEOUT * 2);
        }

        await editor.performKeyCombination(javaFileName, Key.chord(Key.BACK_SPACE));
        await editor.waitErrorInLineDisappearance(30, javaFileName);
    });

    test('Suggestion', async () => {
        await editor.moveCursorToLineAndChar(javaFileName, 32, 21);
        await editor.pressControlSpaceCombination(javaFileName);
        await editor.waitSuggestionWithScrolling(javaFileName, 'run(Class<?> primarySource, String... args) : ConfigurableApplicationContext', 120_000);
    });

    test('Codenavigation', async () => {
        await editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        await editor.performKeyCombination(javaFileName, Key.chord(Key.CONTROL, Key.F12));
        await editor.waitEditorAvailable(codeNavigationClassName, TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT * 4);
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
    test('Build application', async () => {
        const taskName: string = 'build-file-output';
        await topMenu.runTask(`${taskName}, ${globalTaskScope}`);
        await terminal.waitIconSuccess(taskName, 500_000);
    });

    test('Run application', async () => {
        const taskName: string = 'run';
        await topMenu.runTask(`${taskName}, ${globalTaskScope}`);
        await ide.waitNotification('Process 8080-tcp is now listening on port 8080. Open it ?', 120_000);
        await ide.clickOnNotificationButton('Process 8080-tcp is now listening on port 8080. Open it ?', 'Open In Preview');
    });

    test('Check the running application', async () => {
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60_000, 10_000);
    });

    test('Close preview widget', async () => {
        await rightToolBar.clickOnToolIcon('Preview');
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
        const taskName: string = 'build';
        await topMenu.runTask(`${taskName}, ${globalTaskScope}`);
        await terminal.waitIconSuccess(taskName, 250_000);
    });

    test('Run application with changes', async () => {
        const taskName: string = 'run-with-changes';
        await topMenu.runTask(`${taskName}, ${globalTaskScope}`);
        await ide.waitNotification('Process 8080-tcp is now listening on port 8080. Open it ?', 120_000);
        await ide.clickOnNotificationButton('Process 8080-tcp is now listening on port 8080. Open it ?', 'Open In Preview');
    });

    test('Check changes are displayed', async () => {
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60_000, 10_000);
        await checkErrorMessageInApplicationController();
    });

    test('Close preview widget', async () => {
        await rightToolBar.clickOnToolIcon('Preview');
        await previewWidget.waitPreviewWidgetAbsence();
    });

    test('Close running terminal processes and tabs', async () => {
        await terminal.rejectTerminalProcess('run-with-changes');
        await terminal.closeTerminalTab('run-with-changes');
        await warningDialog.waitAndCloseIfAppear();
    });
});

suite('Validation of debug functionality', async () => {
    test('Launch debug', async () => {
        const taskName: string = 'run-debug';
        await topMenu.runTask(`${taskName}, ${globalTaskScope}`);
        await ide.waitNotification('Process 8080-tcp is now listening on port 8080. Open it ?', 180_000);
        await ide.clickOnNotificationButton('Process 8080-tcp is now listening on port 8080. Open it ?', 'Open In Preview');
    });

    test('Check content of the launched application', async () => {
        await previewWidget.waitAndSwitchToWidgetFrame();
        await previewWidget.waitAndClick(SpringAppLocators.springHomeButtonLocator);
        await driverHelper.getDriver().switchTo().defaultContent();
        await ide.waitAndSwitchToIdeFrame();
    });

    test('Open debug view', async () => {
        await projectTree.expandPathAndOpenFile(pathToJavaFolder + '/system', weclomeControllerJavaFileName);
        await editor.selectTab(weclomeControllerJavaFileName);
        await topMenu.selectOption('View', 'Debug');
        await ide.waitLeftToolbarButton(LeftToolbarButton.Debug);
    });

    test('Choose debug configuration', async () => {
        // workaround to the https://github.com/eclipse/che/issues/19887
        await debugView.clickOnDebugConfigurationDropDown();
        await debugView.clickOnDebugConfigurationItem('Add Configuration...');

        await debugView.clickOnDebugConfigurationDropDown();
        await debugView.clickOnDebugConfigurationItem('Debug (Attach) - Remote (petclinic)');
    });

    test('Run debug', async () => {
        await debugView.clickOnRunDebugButton();
        await waitDebugToConnect();
    });

    test('Activate breakpoint', async () => {
        await editor.selectTab(weclomeControllerJavaFileName);
        await editor.activateBreakpoint(weclomeControllerJavaFileName, 27);
    });

    test('Check debugger stop at the breakpoint', async () => {
        await previewWidget.refreshPage();
        await waitStoppedBreakpoint(27);
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

async function waitDebugToConnect() {
    try {
        await debugView.waitForDebuggerToConnect();
    } catch (err) {
        Logger.debug('Workaround for the https://github.com/eclipse/che/issues/18034 issue.');
        await debugView.clickOnThreadsViewTitle();

        await debugView.waitForDebuggerToConnect();
    }
}

async function waitStoppedBreakpoint(lineNumber: number) {
    try {
        await editor.waitStoppedDebugBreakpoint(weclomeControllerJavaFileName, lineNumber);
    } catch (err) {
        await previewWidget.refreshPage();
        await editor.waitStoppedDebugBreakpoint(weclomeControllerJavaFileName, lineNumber);
    }
}
