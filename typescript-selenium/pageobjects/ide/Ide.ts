import { DriverHelper } from "../../utils/DriverHelper";
import { injectable, inject } from "inversify";
import { CLASSES } from "../../types";
import { TestConstants } from "../../TestConstants";
import { By } from "selenium-webdriver";

/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

@injectable()
export class Ide {

    private readonly driverHelper: DriverHelper;

    private static readonly TOP_MENU_PANEL: string = "#theia-app-shell #theia-top-panel .p-MenuBar-content";
    private static readonly LEFT_CONTENT_PANEL: string = "#theia-left-content-panel";
    public static readonly EXPLORER_BUTTON: string = ".theia-app-left .p-TabBar-content li[title='Explorer']";
    private static readonly PRELOADER: string = ".theia-preload";
    private static readonly IDE_IFRAME_CSS: string = "iframe#ide-application-iframe";

    constructor(
        @inject(CLASSES.DriverHelper) driverHelper: DriverHelper
    ) {
        this.driverHelper = driverHelper;
    }

    async waitAndSwitchToIdeFrame(timeout = TestConstants.LOAD_PAGE_TIMEOUT){
        await this.driverHelper.waitAndSwitchToFrame(By.css(Ide.IDE_IFRAME_CSS), timeout)
    }

    async waitNotification(notificationMessage: string, timeout = TestConstants.DEFAULT_TIMEOUT) {
        const notificationLocator: By = By.css(`div[id='notification-container-3-${notificationMessage}-|']`)

        await this.driverHelper.waitVisibility(notificationLocator, timeout)
    }

    async waitNotificationDisappearance(notificationMessage: string, attempts = TestConstants.DEFAULT_ATTEMPTS, polling = TestConstants.DEFAULT_POLLING) {
        const notificationLocator: By = By.css(`div[id='notification-container-3-${notificationMessage}-|']`)

        await this.driverHelper.waitDisappearance(notificationLocator, attempts, polling)
    }


    waitWorkspaceAndIde(workspaceNamespace: string, workspaceName: string) {
        cy.log("**=> Ide.waitWorkspaceAndIde**")
            .then(() => {
                this.testWorkspaceUtil.waitWorkspaceRunning(workspaceNamespace, workspaceName)
            })
            .then(() => {
                [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.EXPLORER_BUTTON]
                    .forEach(idePart => {
                        cy.get(idePart, { timeout: Ide.LOAD_PAGE_TIMEOUT })
                            .should('be.visible')
                    })
            });
    }

    waitIde() {
        [Ide.TOP_MENU_PANEL, Ide.LEFT_CONTENT_PANEL, Ide.EXPLORER_BUTTON]
            .forEach(idePart => {
                cy.get(idePart, { timeout: Ide.LOAD_PAGE_TIMEOUT })
                    .should('be.visible')
            })
    }

    openIdeWithoutFrames(workspaceName: string) {
        cy.log("**=> Ide.openIdeWithoutFrames**")
            .then(() => {
                let workspaceUrl: string = `/che/${workspaceName}`

                cy.visit(workspaceUrl);
            })
    }

    waitExplorerButton() {
        cy.get(Ide.EXPLORER_BUTTON)
            .should('be.visible');
    }

    clickOnExplorerButton() {
        cy.get(Ide.EXPLORER_BUTTON)
            .first()
            .click();
    }

    waitTopMenuPanel() {
        cy.get(Ide.TOP_MENU_PANEL)
            .should('be.visible');
    }

    waitLeftContentPanel() {
        cy.get(Ide.LEFT_CONTENT_PANEL)
            .should('be.visible');
    }

    waitPreloaderAbsent() {
        cy.get(Ide.PRELOADER)
            .should('not.be.visible');
    }

    waitStatusBarContains(expectedText: string) {
        cy.get("div[id='theia-statusBar']", { timeout: Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT })
            .should(elem => {
                let elementText: string = elem[0].innerText.toString();

                expect(elementText).contain(expectedText);
            })

    }

    waitStatusBarTextAbcence(expectedText: string) {
        cy.get("div[id='theia-statusBar']", { timeout: Ide.LANGUAGE_SERVER_INITIALIZATION_TIMEOUT })
            .should(elem => {
                let elementText: string = elem[0].innerText.toString();

                expect(elementText).not.contain(expectedText);
            })
    }






}