// /*********************************************************************
//  * Copyright (c) 2021 Red Hat, Inc.
//  *
//  * This program and the accompanying materials are made
//  * available under the terms of the Eclipse Public License 2.0
//  * which is available at https://www.eclipse.org/legal/epl-2.0/
//  *
//  * SPDX-License-Identifier: EPL-2.0
//  **********************************************************************/
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { CLASSES } from '../../inversify.types';
import { DebugView } from '../../pageobjects/ide/DebugView';
import { DialogWindow } from '../../pageobjects/ide/DialogWindow';
import { DriverHelper } from '../../utils/DriverHelper';
import { e2eContainer } from '../../inversify.config';
import { Editor } from '../../pageobjects/ide/Editor';
import { Ide, LeftToolbarButton } from '../../pageobjects/ide/Ide';
import { Key, error, By } from 'selenium-webdriver';
import { Logger } from '../../utils/Logger';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Terminal } from '../../pageobjects/ide/Terminal';
import { TestConstants } from '../../TestConstants';
import { TimeoutConstants } from '../../TimeoutConstants';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import * as fs from 'fs';
import axios from 'axios';
import CheReporter from '../../driver/CheReporter';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const projectName: string = 'java-spring-petclinic';
const workspaceRootFolderName: string = 'src';
const pathToJavaFolder: string = `${projectName}/${workspaceRootFolderName}/main/java/org/springframework/samples/petclinic`;
const javaFileName: string = 'PetClinicApplication.java';
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const classPathFilename: string = '.classpath';
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const codeNavigationClassName: string = 'SpringApplication.class';
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const globalTaskScope = 'Global';
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const warningDialog: DialogWindow = e2eContainer.get(CLASSES.DialogWindow);
const debugView: DebugView = e2eContainer.get(CLASSES.DebugView);
const welcomeControllerJavaFileName: string = 'WelcomeController.java';


const SpringAppLocators = {
    springTitleLocator: By.xpath('//div[@class=\'container-fluid\']//h2[text()=\'Welcome\']'),
    springMenuButtonLocator: By.css('button[data-target=\'#main-navbar\']'),
    springErrorButtonLocator: By.xpath('//div[@id=\'main-navbar\']//span[text()=\'Error\']'),
    springHomeButtonLocator: By.className('navbar-brand'),
    springErrorMessageLocator: By.xpath(`//h2[text()='Something happened...']`)
};

// this test checks only workspace created from "web-nodejs-sample" https://github.com/devfile/devworkspace-operator/blob/main/samples/flattened_theia-next.yaml.
suite('Workspace creation via factory url', async () => {
    let factoryUrl : string = `${TestConstants.TS_SELENIUM_DEVWORKSPACE_URL}`;
    const workspaceRootFolderName: string = 'src';

    suite('Open factory URL', async () => {
        // this is DevWorkspace test specific - we create the test ws. using factory instead of chectl
        test(`Navigating to factory URL`, async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(projectName);
        });

        test('Register running workspace', async () => {
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            // in this place we do not check 'Do you trust the authors of', 'Yes, I trust' message
            // it  is  DevWorkspace test specific. After consuming the factory the notification does not appear
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(projectName, workspaceRootFolderName);
        });
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
});
    suite('Validation of workspace build and run', async () => {
        test('Build application', async () => {
           const taskName: string = 'maven-build';
           await topMenu.runTask(`${taskName}, ${globalTaskScope}`);
           await terminal.waitIconSuccess(taskName, 500_000);
        });

        test('Run application', async () => {
           const taskName: string = 'run-with-hsqldb';
           await topMenu.runTask(`${taskName}, ${globalTaskScope}`);
           await ide.waitNotification('Process 8080-tcp is now listening on port 8080. Open it ?', 120_000);
            // devWs specific. After running test application we can open it just in the new window.
            // the preview widget is not available yet.
           await ide.clickOnNotificationButton('Process 8080-tcp is now listening on port 8080. Open it ?', 'Open In New Tab');
       });
        // this is DevWorkspace test specific since Theia does not provide yet preview as a widget
        test('Check the running application', async () => {
            await switchAppWindowAndCheck(SpringAppLocators.springTitleLocator);
       });

        test('Close the terminal running tasks', async () => {
            await terminal.rejectTerminalProcess('run-with-hsqldb');
            await terminal.closeTerminalTab('run-with-hsqldb');
            await warningDialog.waitAndCloseIfAppear();
            await terminal.closeTerminalTab('build');
       });
});

    suite('Validation of debug functionality', async () => {
        let urlToPetClinicApp = '';
        test('Launch debug', async () => {
            const taskName: string = 'run-debug';
            await topMenu.runTask(`${taskName}, ${globalTaskScope}`);
            await ide.waitNotification('Process 8080-tcp is now listening on port 8080. Open it ?', 180_000);
            await ide.clickOnNotificationButton('Process 8080-tcp is now listening on port 8080. Open it ?', 'Open In New Tab');
        });

        test('Check content of the launched application', async () => {
            const mainWindowHandle: string = await browserTabsUtil.getCurrentWindowHandle();
            await browserTabsUtil.waitAndSwitchToAnotherWindow(mainWindowHandle, TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT);
            urlToPetClinicApp = await browserTabsUtil.getCurrentUrl();
            await browserTabsUtil.switchToWindow(mainWindowHandle);
        });

        test('Open debug view', async () => {
            await projectTree.expandPathAndOpenFile(pathToJavaFolder + '/system', welcomeControllerJavaFileName);
            await editor.selectTab(welcomeControllerJavaFileName);
            await topMenu.selectOption('View', 'Debug');
            await ide.waitLeftToolbarButton(LeftToolbarButton.Debug);
        });

        test('Choose debug configuration', async () => {
            await debugView.clickOnDebugConfigurationDropDown();
            await debugView.clickOnDebugConfigurationItem('Debug (Attach) - Remote (java-spring-petclinic)');
        });

        test('Run debug', async () => {
            await debugView.clickOnRunDebugButton();
            await waitDebugToConnect();
        });

        test('Activate breakpoint', async () => {
            await editor.selectTab(welcomeControllerJavaFileName);
            await editor.activateBreakpoint(welcomeControllerJavaFileName, 27);
        });

        test('Check debugger stop at the breakpoint', async () => {
            await sendRequestToDebugApp(urlToPetClinicApp);
            await waitStoppedBreakpoint(27);
        });

    });

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

// this is DevWorkspace test specific since Theia does not provide yet preview as a widget
async function switchAppWindowAndCheck(contentLocator: By) {
    const mainWindowHandle: string = await browserTabsUtil.getCurrentWindowHandle();
    await browserTabsUtil.waitAndSwitchToAnotherWindow(mainWindowHandle, TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT);

    // make 3 attempts for waiting application availability with the configured period
    for (let index = 0; index < 3; index++) {
        let isApplicationTitleVisible: boolean = await driverHelper.isVisible(contentLocator);
        if (isApplicationTitleVisible) {
            console.log('----------- The Spring app is available.');
            break;
        } else {
            console.log(`----------- The Spring app is not available. Attempt # ${index}. Waiting ${TimeoutConstants.TS_SELENIUM_DIALOG_WIDGET_TIMEOUT}s`);
            await driverHelper.getDriver().sleep(TimeoutConstants.TS_SELENIUM_DIALOG_WIDGET_TIMEOUT);
            await driverHelper.getDriver().navigate().refresh();
        }
    }
    await driverHelper.waitVisibility(contentLocator);
    await driverHelper.getDriver().close();
    await browserTabsUtil.switchToWindow(mainWindowHandle);
}

async function waitStoppedBreakpoint(lineNumber: number) {
    try {
        await editor.waitStoppedDebugBreakpoint(welcomeControllerJavaFileName, lineNumber);
    } catch (err) {
        const mainWindowHandle: string = await browserTabsUtil.getCurrentWindowHandle();
        await browserTabsUtil.waitAndSwitchToAnotherWindow(mainWindowHandle, TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT);
        await driverHelper.getDriver().navigate().refresh();
        await browserTabsUtil.switchToWindow(mainWindowHandle);
        await editor.waitStoppedDebugBreakpoint(welcomeControllerJavaFileName, lineNumber);
    }
}
// we need to send a request to test app. under debugging because when we try to make it with WebDriver
// the WebDriver will hang. Because the test application does not respond under debugging,
// for avoiding this problem we send http request with axios and set the request timeout. We expect that request will fail with
// timeout error, we check it in the catch block and wait breakpoint activating in the WebDriver after this.
async function sendRequestToDebugApp(urlToApp: string) {
  const httpClient = axios.create();
  httpClient.defaults.timeout = 1000;
    try {
      await httpClient.get(urlToApp);
    } catch (error) {
      if (error.message === 'timeout of 1000ms exceeded') {
      console.log('>>>>The debugger is set >>>>>>>>>>>>>>>>>>> ' + error.message);
      } else {          const {data} = await httpClient.get(urlToApp);
          console.log('>>>>>>>seems the app. is not set under debug properly: >>>>>>>>>>>>>>' + data);
      }
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

