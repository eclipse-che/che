/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { injectable, inject } from "inversify";
import { DriverHelper } from "../../utils/DriverHelper";
import { CLASSES } from "../../inversify.types";
import { TestConstants } from "../../TestConstants";
import { By } from "selenium-webdriver";
import 'reflect-metadata';


@injectable()
export class NewWorkspace {
    private readonly driverHelper: DriverHelper;

    private static readonly CHE_7_STACK_CSS: string = "div[data-stack-id='che7-preview']";
    private static readonly SELECTED_CHE_7_STACK_CSS: string = ".stack-selector-item-selected[data-stack-id='che7-preview']"
    private static readonly CREATE_AND_OPEN_BUTTON_XPATH: string = "(//che-button-save-flat[@che-button-title='Create & Open']/button)[1]"
    private static readonly CREATE_AND_EDIT_BUTTON_CSS: string = "#dropdown-toggle button[name='dropdown-toggle']"
    private static readonly ADD_OR_IMPORT_PROJECT_BUTTON_CSS: string = ".add-import-project-toggle-button";
    private static readonly ADD_BUTTON_CSS: string = "button[aria-disabled='false'][name='addButton']";
    private static readonly NAME_FIELD_CSS: string = "#workspace-name-input";


    constructor(
        @inject(CLASSES.DriverHelper) driverHelper: DriverHelper
    ) {
        this.driverHelper = driverHelper;
    }

    async selectCreateWorkspaceAndProceedEditing(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const createAndProceedEditingButtonLocator: By = By.xpath("//span[text()='Create & Proceed Editing']")

        // open drop down list
        await this.driverHelper.waitAndClick(By.css(NewWorkspace.CREATE_AND_EDIT_BUTTON_CSS), timeout)

        // click on "Create & Proceed Editing" item in the drop down list
        await this.driverHelper.waitAndClick(createAndProceedEditingButtonLocator, timeout)
    }

    async typeWorkspaceName(workspaceName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const workspaceNameFieldLocator: By = By.css(NewWorkspace.NAME_FIELD_CSS)

        await this.driverHelper.enterValue(workspaceNameFieldLocator, workspaceName, timeout)
    }

    async clickOnChe7Stack(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const che7StackLocator: By = By.css(NewWorkspace.CHE_7_STACK_CSS)

        await this.driverHelper.waitAndClick(che7StackLocator)
    }

    async waitChe7StackSelected(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const che7SelectedStackLocator: By = By.css(NewWorkspace.SELECTED_CHE_7_STACK_CSS)

        await this.driverHelper.waitVisibility(che7SelectedStackLocator, timeout)
    }

    async clickOnCreateAndOpenButton(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const ideFrameLocator: By = By.xpath("//ide-iframe[@id='ide-iframe-window' and @aria-hidden='false']")

        await this.driverHelper.waitAndClick(By.xpath(NewWorkspace.CREATE_AND_OPEN_BUTTON_XPATH), timeout)

        // check that the workspace has started to boot
        await this.driverHelper.waitVisibility(ideFrameLocator, timeout)
    }

    async clickOnAddOrImportProjectButton(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const addOrImportProjectButtonLocator: By = By.css(NewWorkspace.ADD_OR_IMPORT_PROJECT_BUTTON_CSS)

        await this.driverHelper.waitAndClick(addOrImportProjectButtonLocator, timeout)
    }

    async waitSampleCheckboxEnabling(sampleName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const enabledSampleCheckboxLocator: By = By.css(`#sample-${sampleName}>md-checkbox[aria-checked='true']`)

        await this.driverHelper.waitVisibility(enabledSampleCheckboxLocator, timeout)
    }

    async enableSampleCheckbox(sampleName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const sampleCheckboxLocator: By = By.xpath(`(//*[@id='sample-${sampleName}']//md-checkbox//div)[1]`)

        await this.driverHelper.waitAndClick(sampleCheckboxLocator, timeout)
        await this.waitSampleCheckboxEnabling(sampleName, timeout)
    }

    async waitProjectAdding(projectName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const addedProjectLocator: By = By.css(`#project-source-selector toggle-single-button#${projectName}`)

        await this.driverHelper.waitVisibility(addedProjectLocator, timeout)
    }

    async waitProjectAbsence(projectName: string, timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        const addedProjectLocator: By = By.css(`#project-source-selector toggle-single-button#${projectName}`)

        await this.driverHelper.waitDisappearance(addedProjectLocator, timeout)
    }

    async clickOnAddButton(timeout = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        await this.driverHelper.waitAndClick(By.css(NewWorkspace.ADD_BUTTON_CSS), timeout)
    }

}
