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
import { By, Key, error } from 'selenium-webdriver';
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


suite('Validation of workspace start', async () => {
    test('Open workspace', async () => {
        await driverHelper.navigateTo(workspaceUrl);
    });

    test('Wait workspace running state', async () => {
        await ide.waitWorkspaceAndIde(namespace, workspaceName);
    });

    test('Wait until project is imported', async () => {
        await projectTree.openProjectTreeContainer();
        await projectTree.waitProjectImported(projectName, 'src');
        await projectTree.expandItem(`/${projectName}`);
    });
});

suite('Language server validation', async () => {
    test('Java LS initialization', async () => {
        await projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        await editor.selectTab(javaFileName);

        await ide.checkLsInitializationStart('Starting Java Language Server');

        await ide.waitStatusBarTextAbsence('Starting Java Language Server', 360000);
        await checkJavaPathCompletion();
        await ide.waitStatusBarTextAbsence('Building workspace', 360000);
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
        await editor.moveCursorToLineAndChar(javaFileName, 32, 27);
        await editor.pressControlSpaceCombination(javaFileName);
        await editor.waitSuggestion(javaFileName, 'run(Class<?> primarySource, String... args) : ConfigurableApplicationContext');
    });

    test('Codenavigation', async () => {
        await editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        await editor.performKeyCombination(javaFileName, Key.chord(Key.CONTROL, Key.F12));
        await editor.waitEditorAvailable(codeNavigationClassName);
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
        await runTask('che: build-file-output');

        await projectTree.expandPathAndOpenFile(projectName, 'build-output.txt');
        await editor.followAndWaitForText('build-output.txt', '[INFO] BUILD SUCCESS', 180000, 5000);
    });

    test('Run application', async () => {
        await runTask('che: run');
        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
    });

    test('Check the running application', async () => {
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60000, 10000);
    });

    test('Close preview widget', async () => {
        await rightToolbar.clickOnToolIcon('Preview');
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

        await editor.moveCursorToLineAndChar(changedJavaFileName, 34, 55);
        await editor.performKeyCombination(changedJavaFileName, textForErrorMessageChange);
        await editor.performKeyCombination(changedJavaFileName, Key.chord(Key.CONTROL, 's'));
    });

    test('Build application with changes', async () => {
        await runTask('che: build');

        await projectTree.expandPathAndOpenFile(projectName, 'build.txt');
        await editor.waitEditorAvailable('build.txt');
        await editor.clickOnTab('build.txt');
        await editor.waitTabFocused('build.txt');
        await editor.followAndWaitForText('build.txt', '[INFO] BUILD SUCCESS', 180000, 5000);
    });

    test('Run application with changes', async () => {
        await runTask('che: run-with-changes');

        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
    });

    test('Check changes are displayed', async () => {
        await previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60000, 10000);
        await previewWidget.waitAndSwitchToWidgetFrame();
        await previewWidget.waitAndClick(SpringAppLocators.springMenuButtonLocator);
        await previewWidget.waitAndClick(SpringAppLocators.springErrorButtonLocator);
        await previewWidget.waitVisibility(SpringAppLocators.springErrorMessageLocator);
        await previewWidget.switchBackToIdeFrame();
    });

    test('Close preview widget', async () => {
        await rightToolbar.clickOnToolIcon('Preview');
        await previewWidget.waitPreviewWidgetAbsence();
    });

    test('Close running terminal processes and tabs', async () => {
        await terminal.rejectTerminalProcess('run-with-changes');
        await terminal.closeTerminalTab('run-with-changes');

        await warningDialog.waitAndCloseIfAppear();
    });
});

suite('Validation of debug functionality', async () => {
    test('Open file and activate breakpoint', async () => {
        await projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        await editor.selectTab(javaFileName);
        await editor.moveCursorToLineAndChar(javaFileName, 34, 1);
        await editor.activateBreakpoint(javaFileName, 32);
    });

    test('Launch debug', async () => {
        await runTask('che: run-debug');

        await ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        await ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
    });

    test('Check content of the launched application', async () => {
        await previewWidget.waitContentAvailable(SpringAppLocators.springErrorMessageLocator, 60000, 10000);
    });

    test('Open debug configuration file', async () => {
        await topMenu.selectOption('Debug', 'Open Configurations');
        await editor.waitEditorAvailable('launch.json');
        await editor.selectTab('launch.json');
    });

    test('Add debug configuration options', async () => {
        await editor.moveCursorToLineAndChar('launch.json', 5, 22);
        await editor.performKeyCombination('launch.json', Key.chord(Key.CONTROL, Key.SPACE));
        await editor.clickOnSuggestion('Java: Launch Program in Current File');
        await editor.waitTabWithUnsavedStatus('launch.json');
        await editor.waitText('launch.json', '\"name\": \"Debug (Launch) - Current File\"');
        await editor.performKeyCombination('launch.json', Key.chord(Key.CONTROL, 's'));
        await editor.waitTabWithSavedStatus('launch.json');
    });

    test('Run debug and check application stop in the breakpoint', async () => {
        await editor.selectTab(javaFileName);
        await topMenu.selectOption('View', 'Debug');
        await ide.waitRightToolbarButton(RightToolbarButton.Debug);
        await debugView.clickOnDebugConfigurationDropDown();
        await debugView.clickOnDebugConfigurationItem('Debug (Launch) - Current File');
        await debugView.clickOnRunDebugButton();

        await previewWidget.refreshPage();
        await editor.waitStoppedDebugBreakpoint(javaFileName, 32);
    });
});

async function checkJavaPathCompletion() {
    if (await ide.isNotificationPresent('Classpath is incomplete. Only syntax errors will be reported')) {
        throw new Error('Known issue: https://github.com/eclipse/che/issues/13427 \n' +
            '\"Java LS \"Classpath is incomplete\" warning when loading petclinic\"');
    }
}

async function runTask(task: string) {
    await topMenu.selectOption('Terminal', 'Run Task...');
    try {
        await quickOpenContainer.waitContainer();
    } catch (err) {
        if (err instanceof error.TimeoutError) {
            console.log(`After clicking to the "Terminal" -> "Run Task ..." the "Quick Open Container" has not been displayed, one more try`);

            await topMenu.selectOption('Terminal', 'Run Task...');
            await quickOpenContainer.waitContainer();
        }
    }

    await quickOpenContainer.clickOnContainerItem(task);
}
