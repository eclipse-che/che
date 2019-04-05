import { is } from "bluebird";

/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export class ElementStateChecker {

    public isVisible(element: JQuery<HTMLElement>): boolean {
        if (element === null || element === undefined) {
            return false;
        }

        return element[0].offsetWidth > 0 &&
            element[0].offsetHeight > 0 &&
            element[0].getClientRects().length > 0
    }

    public isVisibleByLocator(elementLocator: string): boolean {
        return Cypress.$('body').find(elementLocator).length > 0
    }

    public waitVisibility(elementLocator: string, maximumAttempts: number, polling: number): PromiseLike<boolean>{
        let attempt: number = 1
        return this.doWaitVisibility(elementLocator, attempt, maximumAttempts, polling)
    }

    private doWaitVisibility(elementLocator: string, attempt: number, maximumAttempts:number, polling: number): PromiseLike<boolean>{
        return new Cypress.Promise<boolean>((resolve:any, reject:any) => {

            // break loop if exceeded the maximum number of checking attempts
            if (attempt > maximumAttempts) {
                cy.log(`Exceeded the maximum number of checking attempts, the "${elementLocator}" is not visible`)
                resolve(false)
                return
            }

            // break loop if element is visible
            if(this.isVisibleByLocator(elementLocator)){
                resolve(true)
                return
            }

            // retry 
            cy.log(`The "${elementLocator}" is not visible, wait ${polling} miliseconds and try again` )
            cy.log(`Attempt ${attempt} of ${maximumAttempts}`)
            attempt ++
            cy.wait(polling)
            this.doWaitVisibility(elementLocator, attempt, maximumAttempts, polling)
        })
    }

}
