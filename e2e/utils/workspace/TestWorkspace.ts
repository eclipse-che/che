/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import axios from 'axios';
import { TestConstants } from '../../TestConstants';
import fs from 'fs'

export class TestWorkspace {
    private static readonly PATH_TO_CONF_FILE: string = "test-files/che7-java-workspace.json";
    private static readonly API_ENDPOINT: string = TestConstants.TS_SELENIUM_BASE_URL + "/api/";
    private static readonly WORKSPACE_API_URL: string = TestWorkspace.API_ENDPOINT + "workspace";
    private workspaceId: string = "";
    private workspaceIdeUrl: string = "";

    constructor(private readonly workspaceName: string, private workspaceConfig?: any) {
        this.workspaceName = workspaceName;

        if (workspaceConfig) {
            this.workspaceConfig = workspaceConfig;
        }

        if (!workspaceConfig) {
            let fileText: string = fs.readFileSync(TestWorkspace.PATH_TO_CONF_FILE, "utf8")
            this.workspaceConfig = JSON.parse(fileText)
        }

        this.createWorkspace(workspaceName);
    }

    private async createWorkspace(workspaceName: string) {
        this.workspaceConfig.name = workspaceName;

        axios.post(TestWorkspace.WORKSPACE_API_URL, this.workspaceConfig)
            .then(resp => {
                let responceData = resp.data;
                this.workspaceId = responceData.id;
                this.workspaceIdeUrl = responceData.links.ide;
            })
            .then(() => {
                this.startWorkspace();
            });
    };

    getName(): string {
        return this.workspaceName;
    }

    getId(): string {
        return this.workspaceId;
    }

    getIdeUrl(): string {
        return this.workspaceIdeUrl;
    }

    startWorkspace() {
        let workspaceApiUrl: string = `${TestWorkspace.API_ENDPOINT}workspace/${this.getId()}/runtime`;
        axios.post(workspaceApiUrl)
    }

    deleteWorkspace() {
        let workspaceApiUrl: string = `${TestWorkspace.API_ENDPOINT}workspace/${this.getId()}`;
        axios.delete(workspaceApiUrl)

    }

}
