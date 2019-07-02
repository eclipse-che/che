/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { TestConstants } from '../../TestConstants';
import { By } from 'selenium-webdriver';
import 'reflect-metadata';
import { Dashboard } from './Dashboard';
import { Workspaces } from './Workspaces';
import { WorkspaceDetails } from './workspace-details/WorkspaceDetails';
import { TestWorkspaceUtil, Ide, WorkspaceStatus } from '../..';


@injectable()
export class NewWorkspace {
    private static readonly CHE_7_STACK_CSS: string = 'div[data-stack-id=\'che7-preview\']';
    private static readonly SELECTED_CHE_7_STACK_CSS: string = '.stack-selector-item-selected[data-stack-id=\'che7-preview\']';
    private static readonly CREATE_AND_OPEN_BUTTON_XPATH: string = '(//che-button-save-flat[@che-button-title=\'Create & Open\']/button)[1]';
    private static readonly CREATE_AND_EDIT_BUTTON_CSS: string = '#dropdown-toggle button[name=\'dropdown-toggle\']';
    private static readonly ADD_OR_IMPORT_PROJECT_BUTTON_CSS: string = '.add-import-project-toggle-button';
    private static readonly ADD_BUTTON_CSS: string = 'button[aria-disabled=\'false\'][name=\'addButton\']';
    private static readonly NAME_FIELD_CSS: string = '#workspace-name-input';
    private static readonly TITLE_CSS: string = '#New_Workspace';
    private static readonly HIDDEN_LOADER_CSS = 'md-progress-linear.create-workspace-progress[aria-hidden=\'true\']';
    private static readonly PROJECT_SOURCE_FORM_CSS = '#project-source-selector .project-source-selector-popover[aria-hidden=\'false\']';


    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Dashboard) private readonly dashboard: Dashboard,
        @inject(CLASSES.Workspaces) private readonly workspaces: Workspaces,
        @inject(CLASSES.TestWorkspaceUtil) private readonly testWorkspaceUtil: TestWorkspaceUtil,
        @inject(CLASSES.WorkspaceDetails) private readonly workspaceDetails: WorkspaceDetails) { }

    async createAndRunWorkspace(namespace: string, workspaceName: string, dataStackId: string, sampleName: string) {
        await this.prepareWorkspace(workspaceName, dataStackId, sampleName);
        await this.clickOnCreateAndOpenButton();

        await this.waitPageAbsence();
        await this.driverHelper.waitVisibility(By.css(Ide.ACTIVATED_IDE_IFRAME_CSS));
        await this.testWorkspaceUtil.waitWorkspaceStatus(namespace, workspaceName, WorkspaceStatus.STARTING);
    }

    async waitPageAbsence(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.waitDisappearanceWithTimeout(By.css(NewWorkspace.NAME_FIELD_CSS), timeout);
        await this.driverHelper.waitDisappearanceWithTimeout(By.css(NewWorkspace.TITLE_CSS), timeout);
    }

    async createWorkspaceAndProceedEditing(workspaceName: string, dataStackId: string, sampleName: string) {
        await this.prepareWorkspace(workspaceName, dataStackId, sampleName);
        await this.selectCreateWorkspaceAndProceedEditing();

        await this.workspaceDetails.waitPage(workspaceName);
    }

    async createAndOpenWorksapce(workspaceName: string, dataStackId: string, sampleName: string) {
        await this.prepareWorkspace(workspaceName, dataStackId, sampleName);
        await this.clickOnCreateAndOpenButton();

        await this.workspaceDetails.waitPage(workspaceName);
    }

    async confirmProjectAdding(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.clickOnAddButton(timeout);
        await this.waitProjectAdding(sampleName, timeout);
    }

    async waitProjectSourceForm(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(NewWorkspace.PROJECT_SOURCE_FORM_CSS), timeout);
    }

    async selectStack(dataStackId: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const stackLocator: By = By.css(this.getStackCssLocator(dataStackId));

        await this.driverHelper.waitAndClick(stackLocator, timeout);
        await this.waitStackSelection(dataStackId, timeout);
    }

    async waitStackSelection(dataStackId: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const selectedStackLocator: By = By.css(this.getSelectedStackCssLocator(dataStackId));

        await this.driverHelper.waitAndClick(selectedStackLocator, timeout);
    }

    async openPageByUI() {
        await this.dashboard.waitPage();
        await this.dashboard.clickWorkspacesButton();
        await this.workspaces.clickAddWorkspaceButton();

        await this.waitPage();
    }

    async waitPage(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.waitVisibility(By.css(NewWorkspace.NAME_FIELD_CSS), timeout);
        await this.driverHelper.waitVisibility(By.css(NewWorkspace.TITLE_CSS), timeout);
        await this.waitLoaderAbsence(timeout);
    }

    async waitLoaderAbsence(timeout: number = TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        await this.driverHelper.waitPresence(By.css(NewWorkspace.HIDDEN_LOADER_CSS), timeout);
    }

    async selectCreateWorkspaceAndProceedEditing(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const createAndProceedEditingButtonLocator: By = By.xpath('//span[text()=\'Create & Proceed Editing\']');

        // open drop down list
        await this.driverHelper.waitAndClick(By.css(NewWorkspace.CREATE_AND_EDIT_BUTTON_CSS), timeout);

        // click on "Create & Proceed Editing" item in the drop down list
        await this.driverHelper.waitAndClick(createAndProceedEditingButtonLocator, timeout);
    }

    async typeWorkspaceName(workspaceName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const workspaceNameFieldLocator: By = By.css(NewWorkspace.NAME_FIELD_CSS);

        await this.driverHelper.enterValue(workspaceNameFieldLocator, workspaceName, timeout);
    }

    async clickOnChe7Stack(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const che7StackLocator: By = By.css(NewWorkspace.CHE_7_STACK_CSS);

        await this.driverHelper.waitAndClick(che7StackLocator, timeout);
        await this.waitChe7StackSelected(timeout);
    }

    async waitChe7StackSelected(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const che7SelectedStackLocator: By = By.css(NewWorkspace.SELECTED_CHE_7_STACK_CSS);

        await this.driverHelper.waitVisibility(che7SelectedStackLocator, timeout);
    }

    async clickOnCreateAndOpenButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const ideFrameLocator: By = By.xpath('//ide-iframe[@id=\'ide-iframe-window\' and @aria-hidden=\'false\']');

        await this.driverHelper.waitAndClick(By.xpath(NewWorkspace.CREATE_AND_OPEN_BUTTON_XPATH), timeout);

        // check that the workspace has started to boot
        await this.driverHelper.waitVisibility(ideFrameLocator, timeout);
    }

    async clickOnAddOrImportProjectButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const addOrImportProjectButtonLocator: By = By.css(NewWorkspace.ADD_OR_IMPORT_PROJECT_BUTTON_CSS);

        await this.driverHelper.waitAndClick(addOrImportProjectButtonLocator, timeout);
        await this.waitProjectSourceForm(timeout);
    }

    async waitSampleCheckboxEnabling(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const enabledSampleCheckboxLocator: By = By.css(`#sample-${sampleName}>md-checkbox[aria-checked='true']`);

        await this.driverHelper.waitVisibility(enabledSampleCheckboxLocator, timeout);
    }

    async enableSampleCheckbox(sampleName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const sampleCheckboxLocator: By = By.xpath(`(//*[@id='sample-${sampleName}']//md-checkbox//div)[1]`);

        await this.driverHelper.waitAndClick(sampleCheckboxLocator, timeout);
        await this.waitSampleCheckboxEnabling(sampleName, timeout);
    }

    async waitProjectAdding(projectName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const addedProjectLocator: By = By.css(`#project-source-selector toggle-single-button#${projectName}`);

        await this.driverHelper.waitVisibility(addedProjectLocator, timeout);
    }

    async waitProjectAbsence(projectName: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const addedProjectLocator: By = By.css(`#project-source-selector toggle-single-button#${projectName}`);

        await this.driverHelper.waitDisappearance(addedProjectLocator, timeout);
    }

    async clickOnAddButton(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(NewWorkspace.ADD_BUTTON_CSS), timeout);
    }

    private getStackCssLocator(dataStackId: string): string {
        return `span[devfile-name='${dataStackId}']`;
    }

    private getSelectedStackCssLocator(dataStackId: string) {
        return `div.devfile-selector-item-selected[data-devfile-id='${dataStackId}']`;
    }

    private async prepareWorkspace(workspaceName: string, dataStackId: string, sampleName: string) {
        await this.typeWorkspaceName(workspaceName);
        await this.selectStack(dataStackId);
        await this.clickOnAddOrImportProjectButton();
        await this.enableSampleCheckbox(sampleName);
        await this.confirmProjectAdding(sampleName);
    }

}
