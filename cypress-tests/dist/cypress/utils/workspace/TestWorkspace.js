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
var TestWorkspace = /** @class */ (function () {
    function TestWorkspace(workspaceName) {
        this.workspaceName = "";
        this.workspaceId = "";
        this.workspaceIdeUrl = "";
        this.workspaceName = workspaceName;
        this.createWorkspace(workspaceName);
    }
    TestWorkspace.prototype.createWorkspace = function (workspaceName) {
        var _this = this;
        cy.fixture('workspace/che-7-preview.json').then(function (workspaceJson) {
            workspaceJson.name = workspaceName;
            cy.request('POST', TestWorkspace.WORKSPACE_API_URL, workspaceJson);
        }).then(function (response) {
            var responceData = response.body;
            _this.workspaceId = responceData.id;
            _this.workspaceIdeUrl = responceData.links.ide;
        }).then(function () {
            _this.startWorkspace();
        });
    };
    ;
    TestWorkspace.prototype.getName = function () {
        return this.workspaceName;
    };
    TestWorkspace.prototype.getId = function () {
        return this.workspaceId;
    };
    TestWorkspace.prototype.getIdeUrl = function () {
        return this.workspaceIdeUrl;
    };
    TestWorkspace.prototype.startWorkspace = function () {
        var workspaceApiUrl = TestWorkspace.API_ENDPOINT + "workspace/" + this.getId() + "/runtime";
        cy.request('POST', workspaceApiUrl);
    };
    TestWorkspace.prototype.openWorkspaceIde = function () {
        cy.visit(this.workspaceIdeUrl);
    };
    TestWorkspace.prototype.deleteWorkspace = function () {
        var workspaceApiUrl = TestWorkspace.API_ENDPOINT + "workspace/" + this.getId();
        cy.request('DELETE', workspaceApiUrl);
    };
    TestWorkspace.API_ENDPOINT = Cypress.config().baseUrl + "/api/";
    TestWorkspace.WORKSPACE_API_URL = TestWorkspace.API_ENDPOINT + "workspace";
    return TestWorkspace;
}());
export { TestWorkspace };
