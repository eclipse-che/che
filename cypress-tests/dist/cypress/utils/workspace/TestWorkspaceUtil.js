import { Promise } from "bluebird";
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
var TestWorkspaceUtil = /** @class */ (function () {
    function TestWorkspaceUtil() {
    }
    TestWorkspaceUtil.prototype.waitRunningStatus = function (workspaceNamespace, workspaceName, attempt) {
        var _this = this;
        var maximumAttempts = Cypress.env("TestWorkspaceUtil.waitRunningStatusAttempts");
        var delayBetweenAttempts = Cypress.env("TestWorkspaceUtil.waitRunningStatusPollingEvery");
        var expectedWorkspaceStatus = 'RUNNING';
        return new Promise(function (resolve, reject) {
            cy.request('GET', "/api/workspace/" + workspaceNamespace + ":" + workspaceName)
                .then(function (response) {
                if (attempt >= maximumAttempts) {
                    assert.isOk(false, "Exceeded the maximum number of checking attempts, workspace has not been run");
                }
                if (response.status != 200) {
                    cy.log("**Request attempt has responce code '" + response.status + "' diferent to '200' (attempt " + attempt + " of " + maximumAttempts + ")**");
                    cy.wait(delayBetweenAttempts);
                    attempt++;
                    _this.waitRunningStatus(workspaceNamespace, workspaceName, attempt);
                }
                if (response.body.status === expectedWorkspaceStatus) {
                    return;
                }
                cy.log("**Request attempt has workspace status " + response.body.status + " diferent to '" + expectedWorkspaceStatus + "' (attempt " + attempt + " of " + maximumAttempts + ")**");
                cy.wait(delayBetweenAttempts);
                attempt++;
                _this.waitRunningStatus(workspaceNamespace, workspaceName, attempt);
            });
        });
    };
    TestWorkspaceUtil.prototype.waitWorkspaceRunning = function (workspaceNamespace, workspaceName) {
        var attempt = 0;
        return this.waitRunningStatus(workspaceNamespace, workspaceName, attempt);
    };
    return TestWorkspaceUtil;
}());
export { TestWorkspaceUtil };
