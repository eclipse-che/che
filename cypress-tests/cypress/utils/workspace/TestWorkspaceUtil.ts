import { Promise, resolve, reject } from "bluebird";

/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

/// <reference types="Cypress" />


export class TestWorkspaceUtil {

    private waitRunningStatus(workspaceNamespace: string, workspaceName: string, attempt: number): Promise<void> {
        const maximumAttempts: number = Cypress.env("TestWorkspaceUtil.waitRunningStatusAttempts");
        const delayBetweenAttempts: number = Cypress.env("TestWorkspaceUtil.waitRunningStatusPollingEvery");
        const expectedWorkspaceStatus: string = 'RUNNING';

        return new Promise((resolve, reject) => {

            cy.request('GET', `/api/workspace/${workspaceNamespace}:${workspaceName}`)
                .then(response => {

                    if (attempt >= maximumAttempts) {
                        assert.isOk(false, "Exceeded the maximum number of checking attempts, workspace has not been run")
                    }

                    if (response.status != 200) {
                        cy.log(`**Request attempt has responce code '${response.status}' diferent to '200' (attempt ${attempt} of ${maximumAttempts})**`)
                        cy.wait(delayBetweenAttempts);
                        attempt++
                        this.waitRunningStatus(workspaceNamespace, workspaceName, attempt)
                    }

                    if (response.body.status === expectedWorkspaceStatus) {
                        return;
                    }

                    cy.log(`**Request attempt has workspace status ${response.body.status} diferent to '${expectedWorkspaceStatus}' (attempt ${attempt} of ${maximumAttempts})**`)
                    cy.wait(delayBetweenAttempts);
                    attempt++
                    this.waitRunningStatus(workspaceNamespace, workspaceName, attempt)

                })
        })
    }


    waitWorkspaceRunning(workspaceNamespace: string, workspaceName: string): Promise<void> {
        let attempt: number = 0;
        return this.waitRunningStatus(workspaceNamespace, workspaceName, attempt)
    }

}
