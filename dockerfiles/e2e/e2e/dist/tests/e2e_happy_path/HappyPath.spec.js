"use strict";
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
const inversify_config_1 = require("../../inversify.config");
const inversify_types_1 = require("../../inversify.types");
const Ide_1 = require("../../pageobjects/ide/Ide");
const TestConstants_1 = require("../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
const fs = __importStar(require("fs"));
const driverHelper = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.DriverHelper);
const ide = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Ide);
const projectTree = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.ProjectTree);
const topMenu = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.TopMenu);
const quickOpenContainer = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.QuickOpenContainer);
const editor = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Editor);
const previewWidget = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.PreviewWidget);
const rightToolbar = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.RightToolbar);
const terminal = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Terminal);
const debugView = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.DebugView);
const warningDialog = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.WarningDialog);
const projectName = 'petclinic';
const namespace = TestConstants_1.TestConstants.TS_SELENIUM_USERNAME;
const workspaceName = TestConstants_1.TestConstants.TS_SELENIUM_HAPPY_PATH_WORKSPACE_NAME;
const workspaceUrl = `${TestConstants_1.TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/${namespace}/${workspaceName}`;
const pathToJavaFolder = `${projectName}/src/main/java/org/springframework/samples/petclinic`;
const pathToChangedJavaFileFolder = `${projectName}/src/main/java/org/springframework/samples/petclinic/system`;
const classPathFilename = '.classpath';
const javaFileName = 'PetClinicApplication.java';
const changedJavaFileName = 'CrashController.java';
const textForErrorMessageChange = 'HHHHHHHHHHHHH';
const codeNavigationClassName = 'SpringApplication.class';
const pathToYamlFolder = projectName;
const yamlFileName = 'devfile.yaml';
const SpringAppLocators = {
    springTitleLocator: selenium_webdriver_1.By.xpath('//div[@class=\'container-fluid\']//h2[text()=\'Welcome\']'),
    springMenuButtonLocator: selenium_webdriver_1.By.css('button[data-target=\'#main-navbar\']'),
    springErrorButtonLocator: selenium_webdriver_1.By.xpath('//div[@id=\'main-navbar\']//span[text()=\'Error\']'),
    springErrorMessageLocator: selenium_webdriver_1.By.xpath('//p[text()=\'Expected: controller used to ' +
        `showcase what happens when an exception is thrown${textForErrorMessageChange}\']`)
};
suite('Validation of workspace start', () => __awaiter(this, void 0, void 0, function* () {
    test('Open workspace', () => __awaiter(this, void 0, void 0, function* () {
        yield driverHelper.navigateAndWaitToUrl(workspaceUrl);
    }));
    test('Wait workspace running state', () => __awaiter(this, void 0, void 0, function* () {
        yield ide.waitWorkspaceAndIde(namespace, workspaceName);
    }));
    test('Wait until project is imported', () => __awaiter(this, void 0, void 0, function* () {
        yield projectTree.openProjectTreeContainer();
        yield projectTree.waitProjectImported(projectName, 'src');
        yield projectTree.expandItem(`/${projectName}`);
    }));
}));
suite('Language server validation', () => __awaiter(this, void 0, void 0, function* () {
    test('Java LS initialization', () => __awaiter(this, void 0, void 0, function* () {
        yield projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        yield editor.selectTab(javaFileName);
        yield ide.checkLsInitializationStart('Starting Java Language Server');
        yield ide.waitStatusBarTextAbsence('Starting Java Language Server', 360000);
        yield checkJavaPathCompletion();
        yield ide.waitStatusBarTextAbsence('Building workspace', 360000);
    }));
    test('Error highlighting', () => __awaiter(this, void 0, void 0, function* () {
        yield editor.type(javaFileName, 'error', 30);
        yield editor.waitErrorInLine(30);
        yield editor.performKeyCombination(javaFileName, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.BACK_SPACE, selenium_webdriver_1.Key.BACK_SPACE, selenium_webdriver_1.Key.BACK_SPACE, selenium_webdriver_1.Key.BACK_SPACE, selenium_webdriver_1.Key.BACK_SPACE));
        yield editor.waitErrorInLineDisappearance(30);
    }));
    test('Autocomplete', () => __awaiter(this, void 0, void 0, function* () {
        yield editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        yield editor.pressControlSpaceCombination(javaFileName);
        yield editor.waitSuggestionContainer();
        yield editor.waitSuggestion(javaFileName, 'SpringApplication - org.springframework.boot');
    }));
    test('Suggestion', () => __awaiter(this, void 0, void 0, function* () {
        yield editor.moveCursorToLineAndChar(javaFileName, 32, 27);
        yield editor.pressControlSpaceCombination(javaFileName);
        yield editor.waitSuggestion(javaFileName, 'run(Class<?> primarySource, String... args) : ConfigurableApplicationContext');
    }));
    test('Codenavigation', () => __awaiter(this, void 0, void 0, function* () {
        yield editor.moveCursorToLineAndChar(javaFileName, 32, 17);
        yield editor.performKeyCombination(javaFileName, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, selenium_webdriver_1.Key.F12));
        yield editor.waitEditorAvailable(codeNavigationClassName);
    }));
    test.skip('Yaml LS initialization', () => __awaiter(this, void 0, void 0, function* () {
        yield projectTree.expandPathAndOpenFile(pathToYamlFolder, yamlFileName);
        yield editor.waitEditorAvailable(yamlFileName);
        yield editor.clickOnTab(yamlFileName);
        yield editor.waitTabFocused(yamlFileName);
        yield ide.waitStatusBarContains('Starting Yaml Language Server');
        yield ide.waitStatusBarContains('100% Starting Yaml Language Server');
        yield ide.waitStatusBarTextAbsence('Starting Yaml Language Server');
    }));
}));
suite('Validation of workspace build and run', () => __awaiter(this, void 0, void 0, function* () {
    test('Build application', () => __awaiter(this, void 0, void 0, function* () {
        yield runTask('che: build-file-output');
        yield projectTree.expandPathAndOpenFile(projectName, 'build-output.txt');
        yield editor.followAndWaitForText('build-output.txt', '[INFO] BUILD SUCCESS', 180000, 5000);
    }));
    test('Run application', () => __awaiter(this, void 0, void 0, function* () {
        yield runTask('che: run');
        yield ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        yield ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
    }));
    test('Check the running application', () => __awaiter(this, void 0, void 0, function* () {
        yield previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60000, 10000);
    }));
    test('Close preview widget', () => __awaiter(this, void 0, void 0, function* () {
        yield rightToolbar.clickOnToolIcon('Preview');
        yield previewWidget.waitPreviewWidgetAbsence();
    }));
    test('Close the terminal running tasks', () => __awaiter(this, void 0, void 0, function* () {
        yield terminal.closeTerminalTab('build-file-output');
        yield terminal.rejectTerminalProcess('run');
        yield terminal.closeTerminalTab('run');
        yield warningDialog.waitAndCloseIfAppear();
    }));
}));
suite('Display source code changes in the running application', () => __awaiter(this, void 0, void 0, function* () {
    test('Change source code', () => __awaiter(this, void 0, void 0, function* () {
        yield projectTree.expandPathAndOpenFile(pathToChangedJavaFileFolder, changedJavaFileName);
        yield editor.waitEditorAvailable(changedJavaFileName);
        yield editor.clickOnTab(changedJavaFileName);
        yield editor.waitTabFocused(changedJavaFileName);
        yield editor.moveCursorToLineAndChar(changedJavaFileName, 34, 55);
        yield editor.performKeyCombination(changedJavaFileName, textForErrorMessageChange);
        yield editor.performKeyCombination(changedJavaFileName, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, 's'));
    }));
    test('Build application with changes', () => __awaiter(this, void 0, void 0, function* () {
        yield runTask('che: build');
        yield projectTree.expandPathAndOpenFile(projectName, 'build.txt');
        yield editor.waitEditorAvailable('build.txt');
        yield editor.clickOnTab('build.txt');
        yield editor.waitTabFocused('build.txt');
        yield editor.followAndWaitForText('build.txt', '[INFO] BUILD SUCCESS', 180000, 5000);
    }));
    test('Run application with changes', () => __awaiter(this, void 0, void 0, function* () {
        yield runTask('che: run-with-changes');
        yield ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        yield ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
    }));
    test('Check changes are displayed', () => __awaiter(this, void 0, void 0, function* () {
        yield previewWidget.waitContentAvailable(SpringAppLocators.springTitleLocator, 60000, 10000);
        yield previewWidget.waitAndSwitchToWidgetFrame();
        yield previewWidget.waitAndClick(SpringAppLocators.springMenuButtonLocator);
        yield previewWidget.waitAndClick(SpringAppLocators.springErrorButtonLocator);
        yield previewWidget.waitVisibility(SpringAppLocators.springErrorMessageLocator);
        yield previewWidget.switchBackToIdeFrame();
    }));
    test('Close preview widget', () => __awaiter(this, void 0, void 0, function* () {
        yield rightToolbar.clickOnToolIcon('Preview');
        yield previewWidget.waitPreviewWidgetAbsence();
    }));
    test('Close running terminal processes and tabs', () => __awaiter(this, void 0, void 0, function* () {
        yield terminal.rejectTerminalProcess('run-with-changes');
        yield terminal.closeTerminalTab('run-with-changes');
        yield warningDialog.waitAndCloseIfAppear();
    }));
}));
suite('Validation of debug functionality', () => __awaiter(this, void 0, void 0, function* () {
    test('Open file and activate breakpoint', () => __awaiter(this, void 0, void 0, function* () {
        yield projectTree.expandPathAndOpenFile(pathToJavaFolder, javaFileName);
        yield editor.selectTab(javaFileName);
        yield editor.moveCursorToLineAndChar(javaFileName, 34, 1);
        yield editor.activateBreakpoint(javaFileName, 32);
    }));
    test('Launch debug', () => __awaiter(this, void 0, void 0, function* () {
        yield runTask('che: run-debug');
        yield ide.waitNotificationAndConfirm('A new process is now listening on port 8080', 120000);
        yield ide.waitNotificationAndOpenLink('Redirect is now enabled on port 8080', 120000);
    }));
    test('Check content of the launched application', () => __awaiter(this, void 0, void 0, function* () {
        yield previewWidget.waitContentAvailable(SpringAppLocators.springErrorMessageLocator, 60000, 10000);
    }));
    test('Open debug configuration file', () => __awaiter(this, void 0, void 0, function* () {
        yield topMenu.selectOption('Debug', 'Open Configurations');
        yield editor.waitEditorAvailable('launch.json');
        yield editor.selectTab('launch.json');
    }));
    test('Add debug configuration options', () => __awaiter(this, void 0, void 0, function* () {
        yield editor.moveCursorToLineAndChar('launch.json', 5, 22);
        yield editor.performKeyCombination('launch.json', selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, selenium_webdriver_1.Key.SPACE));
        yield editor.clickOnSuggestion('Java: Launch Program in Current File');
        yield editor.waitTabWithUnsavedStatus('launch.json');
        yield editor.waitText('launch.json', '\"name\": \"Debug (Launch) - Current File\"');
        yield editor.waitTabWithSavedStatus('launch.json');
    }));
    test('Run debug and check application stop in the breakpoint', () => __awaiter(this, void 0, void 0, function* () {
        yield editor.selectTab(javaFileName);
        yield topMenu.selectOption('View', 'Debug');
        yield ide.waitRightToolbarButton(Ide_1.RightToolbarButton.Debug);
        yield debugView.clickOnDebugConfigurationDropDown();
        yield debugView.clickOnDebugConfigurationItem('Debug (Launch) - Current File');
        yield debugView.clickOnRunDebugButton();
        yield previewWidget.refreshPage();
        yield editor.waitStoppedDebugBreakpoint(javaFileName, 32);
    }));
}));
function runTask(task) {
    return __awaiter(this, void 0, void 0, function* () {
        yield topMenu.selectOption('Terminal', 'Run Task...');
        try {
            yield quickOpenContainer.waitContainer();
        }
        catch (err) {
            if (err instanceof selenium_webdriver_1.error.TimeoutError) {
                console.log(`After clicking to the "Terminal" -> "Run Task ..." the "Quick Open Container" has not been displayed, one more try`);
                yield topMenu.selectOption('Terminal', 'Run Task...');
                yield quickOpenContainer.waitContainer();
            }
        }
        yield quickOpenContainer.clickOnContainerItem(task);
    });
}
function checkJavaPathCompletion() {
    return __awaiter(this, void 0, void 0, function* () {
        if (yield ide.isNotificationPresent('Classpath is incomplete. Only syntax errors will be reported')) {
            const classpathText = fs.readFileSync('./files/happy-path/petclinic-classpath.txt', 'utf8');
            const workaroundReportText = '\n############################## \n\n' +
                'Known issue: https://github.com/eclipse/che/issues/13427 \n' +
                '\"Java LS \"Classpath is incomplete\" warning when loading petclinic\" \n' +
                '\".classpath\" will be configured with next settings: \n\n' +
                classpathText + '\n' +
                '############################## \n';
            console.log(workaroundReportText);
            yield projectTree.expandPathAndOpenFile(projectName, classPathFilename);
            yield editor.waitEditorAvailable(classPathFilename);
            yield editor.type(classPathFilename, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, 'a'), 1);
            yield editor.performKeyCombination(classPathFilename, selenium_webdriver_1.Key.DELETE);
            yield editor.type(classPathFilename, classpathText, 1);
            yield editor.waitTabWithSavedStatus(classPathFilename);
        }
    });
}
