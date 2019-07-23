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
import { Ide, RightToolbarButton } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { TopMenu } from '../../pageobjects/ide/TopMenu';
import { QuickOpenContainer } from '../../pageobjects/ide/QuickOpenContainer';
import { Editor } from '../../pageobjects/ide/Editor';
import { PreviewWidget } from '../../pageobjects/ide/PreviewWidget';
import { TestConstants } from '../../TestConstants';
import { RightToolbar } from '../../pageobjects/ide/RightToolbar';
import { By, Key } from 'selenium-webdriver';
import { Terminal } from '../../pageobjects/ide/Terminal';
import { DebugView } from '../../pageobjects/ide/DebugView';
import { WarningDialog } from '../../pageobjects/ide/WarningDialog';

const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const quickOpenContainer: QuickOpenContainer = e2eContainer.get(CLASSES.QuickOpenContainer);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const previewWidget: PreviewWidget = e2eContainer.get(CLASSES.PreviewWidget);
const rightToolbar: RightToolbar = e2eContainer.get(CLASSES.RightToolbar);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const debugView: DebugView = e2eContainer.get(CLASSES.DebugView);
const warningDialog: WarningDialog = e2eContainer.get(CLASSES.WarningDialog);

const projectName: string = 'petclinic';
const namespace: string = TestConstants.TS_SELENIUM_USERNAME;
const workspaceName: string = TestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME;
const workspaceUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${namespace}/${workspaceName}`;
const pathToJavaFolder: string = `${projectName}/src/main/java/org/springframework/samples/petclinic`;
const pathToChangedJavaFileFolder: string = `${projectName}/src/main/java/org/springframework/samples/petclinic/system`;
const javaFileName: string = 'PetClinicApplication.java';
const changedJavaFileName: string = 'CrashController.java';
const textForErrorMessageChange: string = 'HHHHHHHHHHHHH';
const codeNavigationClassName: string = 'SpringApplication.class';
const pathToYamlFolder: string = projectName;
const yamlFileName: string = 'devfile.yaml';

const SpringAppLocators = {
    springTitleLocator: By.xpath('//div[@class=\'container-fluid\']//h2[text()=\'Welcome\']'),
    springMenuButtonLocator: By.css('button[data-target=\'#main-navbar\']'),
    springErrorButtonLocator: By.xpath('//div[@id=\'main-navbar\']//span[text()=\'Error\']'),
    springErrorMessageLocator: By.xpath('//p[text()=\'Expected: controller used to ' +
        `showcase what happens when an exception is thrown${textForErrorMessageChange}\']`)
};


suite('Validation of workspace start, build and run', async () => {
    test('Open workspace', async () => {
        await driverHelper.navigateTo(workspaceUrl);
    });

    test('Wait workspace running state', async () => {
        await ide.waitWorkspaceAndIde(namespace, workspaceName);
    });

    test('Wait until project is imported', async () => {
        console.log('============>>>>>  1');
        await projectTree.openProjectTreeContainer();
        console.log('============>>>>>  2');
        await projectTree.waitProjectImported(projectName, 'src');


        // ----------------------------
        await driverHelper.getDriver().navigate().refresh();
        await ide.waitAndSwitchToIdeFrame();
        // ----------------------------


        console.log('============>>>>>  3');
        await projectTree.expandItem(`/${projectName}`);
        console.log('============>>>>>  4');
    });

    test('Build application', async () => {
        console.log('============>>>>>  1');
        await topMenu.selectOption('Terminal', 'Run Task...');
        console.log('============>>>>>  2');
        await quickOpenContainer.clickOnContainerItem('che: build-file-output');

        console.log('============>>>>>  3');
        await projectTree.expandPathAndOpenFile(projectName, 'build-output.txt');
        console.log('============>>>>>  4');
        await editor.followAndWaitForText('build-output.txt', '[INFO] BUILD SUCCESS', 180000, 5000);
        console.log('============>>>>>  5');
    });

    test('Run application', async () => {
        console.log('============>>>>>  1');
        await topMenu.selectOption('Terminal', 'Run Task...');
        console.log('============>>>>>  2');
        await quickOpenContainer.clickOnContainerItem('che: run');

        console.log('============>>>>>  3');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        console.log('============>>>>>  4');
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
        console.log('============>>>>>  5');
    });

    test('Check the running application', async () => {
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60000, 10000);
    });

    test('Close preview widget', async () => {
        console.log('============>>>>>  1');
        await rightToolbar.clickOnToolIcon('Preview');
        console.log('============>>>>>  2');
        await previewWidget.waitPreviewWidgetAbsence();
    });

    test('Close the terminal running tasks', async () => {
        console.log('============>>>>>  1');
        await terminal.closeTerminalTab('build-file-output');
        console.log('============>>>>>  2');
        await terminal.rejectTerminalProcess('run');
        console.log('============>>>>>  3');
        await terminal.closeTerminalTab('run');

        console.log('============>>>>>  4');
        await warningDialog.waitAndCloseIfAppear();
        console.log('============>>>>>  5');
    });
});

suite('Language server validation', async () => {
    test('Java LS initialization', async () => {
        console.log('============>>>>>  1');
        await projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        console.log('============>>>>>  2');
        await editor.selectTab(javaFileName);


        console.log('============>>>>>  3');
        await ide.checkLsInitializationStart('Starting Java Language Server');


        console.log('============>>>>>  4');
        await ide.waitStatusBarTextAbsence('Starting Java Language Server', 360000);
        console.log('============>>>>>  5');
        await checkJavaPathCompletion();
        console.log('============>>>>>  6');
        await ide.waitStatusBarTextAbsence('Building workspace', 360000);
        console.log('============>>>>>  7');
    });

    test('Error highlighting', async () => {
        console.log('============>>>>>  1');
        await editor.type(javaFileName, 'error', 30);
        console.log('============>>>>>  2');
        await editor.waitErrorInLine(30);
        console.log('============>>>>>  3');
        await editor.performKeyCombination(javaFileName, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
        console.log('============>>>>>  4');
        await editor.waitErrorInLineDisappearance(30);
        console.log('============>>>>>  5');
    });

    test('Autocomplete', async () => {
        console.log('============>>>>>  1');
        await editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        console.log('============>>>>>  2');
        await editor.pressControlSpaceCombination(javaFileName);
        console.log('============>>>>>  3');
        await editor.waitSuggestionContainer();
        console.log('============>>>>>  4');
        await editor.waitSuggestion(javaFileName, 'SpringApplication - org.springframework.boot');
        console.log('============>>>>>  5');
    });

    test('Suggestion', async () => {
        console.log('============>>>>>  1');
        await editor.moveCursorToLineAndChar(javaFileName, 32, 27);
        console.log('============>>>>>  2');
        await editor.pressControlSpaceCombination(javaFileName);
        console.log('============>>>>>  3');
        await editor.waitSuggestion(javaFileName, 'run(Class<?> primarySource, String... args) : ConfigurableApplicationContext');
        console.log('============>>>>>  4');
    });

    test('Codenavigation', async () => {
        console.log('============>>>>>  1');
        await editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        console.log('============>>>>>  2');
        await editor.performKeyCombination(javaFileName, Key.chord(Key.CONTROL, Key.F12));
        console.log('============>>>>>  3');
        await editor.waitEditorAvailable(codeNavigationClassName);
        console.log('============>>>>>  4');
    });

    test.skip('Yaml LS initialization', async () => {
        console.log('============>>>>>  1');
        await projectTree.expandPathAndOpenFile(pathToYamlFolder, yamlFileName);
        console.log('============>>>>>  2');
        await editor.waitEditorAvailable(yamlFileName);
        console.log('============>>>>>  3');
        await editor.clickOnTab(yamlFileName);
        console.log('============>>>>>  4');
        await editor.waitTabFocused(yamlFileName);
        console.log('============>>>>>  5');
        await ide.waitStatusBarContains('Starting Yaml Language Server');
        console.log('============>>>>>  6');
        await ide.waitStatusBarContains('100% Starting Yaml Language Server');
        console.log('============>>>>>  7');
        await ide.waitStatusBarTextAbsence('Starting Yaml Language Server');
        console.log('============>>>>>  8');
    });
});

suite('Display source code changes in the running application', async () => {
    test('Change source code', async () => {
        console.log('============>>>>>  1');
        await projectTree.expandPathAndOpenFile(pathToChangedJavaFileFolder, changedJavaFileName);
        console.log('============>>>>>  2');
        await editor.waitEditorAvailable(changedJavaFileName);
        console.log('============>>>>>  3');
        await editor.clickOnTab(changedJavaFileName);
        console.log('============>>>>>  4');
        await editor.waitTabFocused(changedJavaFileName);

        console.log('============>>>>>  5');
        await editor.moveCursorToLineAndChar(changedJavaFileName, 34, 55);
        console.log('============>>>>>  6');
        await editor.performKeyCombination(changedJavaFileName, textForErrorMessageChange);
        console.log('============>>>>>  7');
        await editor.performKeyCombination(changedJavaFileName, Key.chord(Key.CONTROL, 's'));
        console.log('============>>>>>  8');
    });

    test('Build application with changes', async () => {
        console.log('============>>>>>  1');
        await topMenu.selectOption('Terminal', 'Run Task...');
        console.log('============>>>>>  2');
        await quickOpenContainer.clickOnContainerItem('che: build');

        console.log('============>>>>>  3');
        await projectTree.expandPathAndOpenFile(projectName, 'build.txt');
        console.log('============>>>>>  4');
        await editor.waitEditorAvailable('build.txt');
        console.log('============>>>>>  5');
        await editor.clickOnTab('build.txt');
        console.log('============>>>>>  6');
        await editor.waitTabFocused('build.txt');
        console.log('============>>>>>  7');
        await editor.followAndWaitForText('build.txt', '[INFO] BUILD SUCCESS', 180000, 5000);
        console.log('============>>>>>  8');
    });

    test('Run application with changes', async () => {
        console.log('============>>>>>  1');
        await topMenu.selectOption('Terminal', 'Run Task...');
        console.log('============>>>>>  2');
        await quickOpenContainer.clickOnContainerItem('che: run-with-changes');

        console.log('============>>>>>  3');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        console.log('============>>>>>  4');
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
        console.log('============>>>>>  5');
    });

    test('Check changes are displayed', async () => {
        console.log('============>>>>>  1');
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60000, 10000);
        console.log('============>>>>>  2');
        await previewWidget.waitAndSwitchToWidgetFrame();
        console.log('============>>>>>  3');
        await previewWidget.waitAndClick(SpringAppLocators.springMenuButtonLocator);
        console.log('============>>>>>  4');
        await previewWidget.waitAndClick(SpringAppLocators.springErrorButtonLocator);
        console.log('============>>>>>  5');
        await previewWidget.waitVisibility(SpringAppLocators.springErrorMessageLocator);
        console.log('============>>>>>  6');
        await previewWidget.switchBackToIdeFrame();
        console.log('============>>>>>  8');
    });

    test('Close preview widget', async () => {
        console.log('============>>>>>  1');
        await rightToolbar.clickOnToolIcon('Preview');
        console.log('============>>>>>  2');
        await previewWidget.waitPreviewWidgetAbsence();
        console.log('============>>>>>  3');
    });

    test('Close running terminal processes and tabs', async () => {
        console.log('============>>>>>  1');
        await terminal.rejectTerminalProcess('run-with-changes');
        console.log('============>>>>>  2');
        await terminal.closeTerminalTab('run-with-changes');

        console.log('============>>>>>  3');
        await warningDialog.waitAndCloseIfAppear();
        console.log('============>>>>>  4');
    });
});

suite('Validation of debug functionality', async () => {
    test('Open file and activate breakpoint', async () => {
        console.log('============>>>>>  1');
        await projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        console.log('============>>>>>  2');
        await editor.selectTab(javaFileName);
        console.log('============>>>>>  3');
        await editor.moveCursorToLineAndChar(javaFileName, 34, 1);
        console.log('============>>>>>  4');
        await editor.activateBreakpoint(javaFileName, 32);
        console.log('============>>>>>  5');
    });

    test('Launch debug', async () => {
        console.log('============>>>>>  1');
        await topMenu.selectOption('Terminal', 'Run Task...');
        console.log('============>>>>>  2');
        await quickOpenContainer.clickOnContainerItem('che: run-debug');

        console.log('============>>>>>  3');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        console.log('============>>>>>  4');
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
        console.log('============>>>>>  5');
    });

    test('Check content of the launched application', async () => {
        await previewWidget.waitContentAvailable(SpringAppLocators.springErrorMessageLocator, 60000, 10000);
    });

    test('Open debug configuration file', async () => {
        console.log('============>>>>>  1');
        await topMenu.selectOption('Debug', 'Open Configurations');
        console.log('============>>>>>  2');
        await editor.waitEditorAvailable('launch.json');
        console.log('============>>>>>  3');
        await editor.selectTab('launch.json');
        console.log('============>>>>>  4');
    });

    test('Add debug configuration options', async () => {
        console.log('============>>>>>  1');
        await editor.moveCursorToLineAndChar('launch.json', 5, 22);
        console.log('============>>>>>  2');
        await editor.performKeyCombination('launch.json', Key.chord(Key.CONTROL, Key.SPACE));
        console.log('============>>>>>  3');
        await editor.clickOnSuggestion('Java: Launch Program in Current File');
        console.log('============>>>>>  4');
        await editor.waitTabWithUnsavedStatus('launch.json');
        console.log('============>>>>>  5');
        await editor.waitText('launch.json', '\"name\": \"Debug (Launch) - Current File\"');
        console.log('============>>>>>  6');
        await editor.performKeyCombination('launch.json', Key.chord(Key.CONTROL, 's'));
        console.log('============>>>>>  7');
        await editor.waitTabWithSavedStatus('launch.json');
        console.log('============>>>>>  8');
    });

    test('Run debug and check application stop in the breakpoint', async () => {
        console.log('============>>>>>  1');
        await editor.selectTab(javaFileName);
        console.log('============>>>>>  2');
        await topMenu.selectOption('View', 'Debug');
        console.log('============>>>>>  3');
        await ide.waitRightToolbarButton(RightToolbarButton.Debug);
        console.log('============>>>>>  4');
        await debugView.clickOnDebugConfigurationDropDown();
        console.log('============>>>>>  5');
        await debugView.clickOnDebugConfigurationItem('Debug (Launch) - Current File');
        console.log('============>>>>>  6');
        await debugView.clickOnRunDebugButton();

        console.log('============>>>>>  7');
        await previewWidget.refreshPage();
        console.log('============>>>>>  8');
        await editor.waitStoppedDebugBreakpoint(javaFileName, 32);
        console.log('============>>>>>  9');
    });
});

async function checkJavaPathCompletion() {
    if (await ide.isNotificationPresent('Classpath is incomplete. Only syntax errors will be reported')) {
        throw new Error('Known issue: https://github.com/eclipse/che/issues/13427 \n' +
            '\"Java LS \"Classpath is incomplete\" warning when loading petclinic\"');
    }
}
