import { ElementStateChecker } from "../../../utils/ElementStateChecker";

/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

 
export class WorkspaceDetailsPlugins{
    private readonly elementStateChecker: ElementStateChecker = new ElementStateChecker();
    

    private getPluginListItemLocator(pluginName: string): string {
        return `.plugin-item div[plugin-item-name='${pluginName}']`
    }

    private getPluginListItemSwitcherLocator(pluginName: string): string {
        return `${this.getPluginListItemLocator(pluginName)} md-switch`
    }

    waitPluginListItem(pluginName: string) {
        cy.get(this.getPluginListItemLocator(pluginName))
            .should(element => {
                expect(this.elementStateChecker.isVisible(element)).to.be.true
            })
    }

    clickOnPluginListItemSwitcher(pluginName: string) {
        cy.get(this.getPluginListItemSwitcherLocator(pluginName))
            .should(element => {
                expect(this.elementStateChecker.isVisible(element)).to.be.true
            })
            .click({ force: true })
    }

    waitPluginEnabling(pluginName: string) {
        cy.get(this.getPluginListItemSwitcherLocator(pluginName))
            .should(element => {
                expect(this.elementStateChecker.isVisible(element)).to.be.true
            })
            .should('have.attr', 'aria-checked', 'true')
    }

    waitPluginDisabling(pluginName: string) {
        cy.get(this.getPluginListItemSwitcherLocator(pluginName))
            .should(element => {
                expect(this.elementStateChecker.isVisible(element)).to.be.true
            })
            .should('have.attr', 'aria-checked', 'false')
    }

}
