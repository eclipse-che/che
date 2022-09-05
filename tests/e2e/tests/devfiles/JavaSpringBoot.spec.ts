/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { CLASSES } from '../../inversify.types';
import { LanguageServerTests } from '../../testsLibrary/LanguageServerTests';
import { e2eContainer } from '../../inversify.config';
import { CodeExecutionTests } from '../../testsLibrary/CodeExecutionTests';
import { ProjectAndFileTests } from '../../testsLibrary/ProjectAndFileTests';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../driver/CheReporter';
import { CreateWorkspace } from '../../pageobjects/dashboard/CreateWorkspace';
import { Logger } from '../../utils/Logger';
import { ApiUrlResolver } from '../../utils/workspace/ApiUrlResolver';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { DriverHelper } from '../../utils/DriverHelper';
import { By, until } from 'selenium-webdriver';
import { Workbench } from 'monaco-page-objects';

const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);
const projectAndFileTests: ProjectAndFileTests = e2eContainer.get(CLASSES.ProjectAndFileTests);
const commonLanguageServerTests: LanguageServerTests = e2eContainer.get(CLASSES.LanguageServerTests);
const codeExecutionTests: CodeExecutionTests = e2eContainer.get(CLASSES.CodeExecutionTests);
const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);
const createWorkspace: CreateWorkspace = e2eContainer.get(CLASSES.CreateWorkspace);
const apiUrlResolver: ApiUrlResolver = e2eContainer.get(CLASSES.ApiUrlResolver);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

const stack: string = 'Java Spring Boot';
const workspaceSampleName: string = 'java-spring-petclinic';
const workspaceRootFolderName: string = 'src';
const fileFolderPath: string = `${workspaceSampleName}/${workspaceRootFolderName}/main/java/org/springframework/samples/petclinic`;
const tabTitle: string = 'PetClinicApplication.java';
const codeNavigationClassName: string = 'SpringApplication.class';
const buildTaskName: string = 'maven build';
const runTaskName: string = 'run webapp';
const runTaskExpectedDialogue: string = 'Process 8080-tcp is now listening on port 8080. Open it ?';

suite(`${stack} test`, async () => {
    suite(`Create ${stack} workspace`, async () => {
        // workspaceHandlingTests.createAndOpenWorkspace(stack);
        test('Start Maven workspace using factory URL and vscode editor', async() => {
            await dashboard.waitPage();
            Logger.debug(`Fetching user kubernetes namespace, storing auth token by getting workspaces API URL.`);
            await apiUrlResolver.getWorkspacesApiUrl();
            await dashboard.clickCreateWorkspaceButton();
            await createWorkspace.waitPage();
            workspaceHandlingTests.setWindowHandle(await browserTabsUtil.getCurrentWindowHandle());
            await createWorkspace.startWorkspaceUsingFactory(`https://github.com/che-samples/java-spring-petclinic/tree/devfilev2?che-editor=che-incubator/che-code/insiders&storageType=persistent`);
            await browserTabsUtil.waitAndSwitchToAnotherWindow(workspaceHandlingTests.getWindowHandle(), TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
        });
        workspaceHandlingTests.obtainWorkspaceNameFromStartingPage();
        test('Register running workspace', async () => {
            CheReporter.registerRunningWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
        test('Wait workspace readiness', async() => {
            try {
                await driverHelper.getDriver().wait(until.elementLocated(By.className('monaco-workbench')));
            } catch (err) {
                if ((err as Error).name === 'WebDriverError') {
                    await new Promise(res => setTimeout(res, 3000));
                } else {
                    throw err;
                }
            }
            let workbench = new Workbench();
            let activityBar = workbench.getActivityBar();
            let activityBarControls = await activityBar.getViewControls();
            // let sidebarContent = activityBar.getViewControl();
            // let sidebarViewSections = await sidebarContent.getSections();
            Logger.debug(`Editor sections:`);
            activityBarControls.forEach(async control => {
                Logger.debug(`${await control.getTitle()}`);
            });
        });
        // projectAndFileTests.waitWorkspaceReadiness(workspaceSampleName, workspaceRootFolderName, false);
    });

    suite.skip('Test opening file', async () => {
        // opening file that soon should give time for LS to initialize
        projectAndFileTests.openFile(fileFolderPath, tabTitle);
    });

    suite.skip('Validation of workspace build', async () => {
        codeExecutionTests.runTask(buildTaskName, 720_000);
        codeExecutionTests.closeTerminal(buildTaskName);
    });

    suite.skip('Validation of workspace execution', async () => {
        codeExecutionTests.runTaskWithNotification(runTaskName, runTaskExpectedDialogue, 120_000);
        codeExecutionTests.closeTerminal(runTaskName);
    });

    suite.skip('Language server validation', async () => {
        commonLanguageServerTests.autocomplete(tabTitle, 32, 56, 'args : String[]');
        commonLanguageServerTests.errorHighlighting(tabTitle, 'error_text', 30);
        commonLanguageServerTests.goToImplementations(tabTitle, 32, 23, codeNavigationClassName);
        commonLanguageServerTests.suggestionInvoking(tabTitle, 32, 23, 'run(Class<?>');
    });

    suite('Stopping and deleting the workspace', async () => {
        test(`Stop and remowe workspace`, async () => {
            await workspaceHandlingTests.stopAndRemoveWorkspace(WorkspaceHandlingTests.getWorkspaceName());
        });
    });
});
