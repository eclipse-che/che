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

    private waitRunningStatus(workspaceNamespace: string, workspaceName: string, attempt: number): PromiseLike<void> {
        const maximumAttempts: number = Cypress.env("TestWorkspaceUtil.waitRunningStatusAttempts");
        const delayBetweenAttempts: number = Cypress.env("TestWorkspaceUtil.waitRunningStatusPollingEvery");
        const runningWorkspaceStatus: string = 'RUNNING';
        const stoppedWorkspaceStatus: string = 'STOPPED';
        const startingWorkspaceStatus: string = 'STARTING';


        return new Cypress.Promise((resolve:any, reject:any) => {
            let isWorkspaceStarting: boolean = false;

            cy.request('GET', `/api/workspace/${workspaceNamespace}:${workspaceName}`)
                .then(response => {

                    if (attempt > maximumAttempts) {
                        assert.isOk(false, "Exceeded the maximum number of checking attempts, workspace has not been run")
                    }

                    if (response.status != 200) {
                        cy.log(`**Request attempt has responce code '${response.status}' diferent to '200'**`)
                        cy.log(`**Attempt ${attempt} of ${maximumAttempts}**`)
                        cy.wait(delayBetweenAttempts);
                        attempt++
                        this.waitRunningStatus(workspaceNamespace, workspaceName, attempt)
                    }

                    let workspaceStatus: string = response.body.status

                    if (workspaceStatus === runningWorkspaceStatus) {
                        return;
                    }

                    if(workspaceStatus === startingWorkspaceStatus){
                        isWorkspaceStarting = true;
                    }

                    if((workspaceStatus === stoppedWorkspaceStatus) && isWorkspaceStarting){
                        assert.isOk(false, "Workspace starting process is crushed")
                    }

                    cy.log(`**Request attempt has workspace status '${response.body.status}' diferent to '${runningWorkspaceStatus}'**`)
                    cy.log(`**Attempt ${attempt} of ${maximumAttempts}**`)
                    cy.wait(delayBetweenAttempts);
                    attempt++
                    this.waitRunningStatus(workspaceNamespace, workspaceName, attempt)

                })
        })
    }


    waitWorkspaceRunning(workspaceNamespace: string, workspaceName: string): PromiseLike<void> {
        let attempt: number = 1;
        return this.waitRunningStatus(workspaceNamespace, workspaceName, attempt)
    }

}
