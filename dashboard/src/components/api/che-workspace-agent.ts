/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {CheProject} from './che-project';
import {CheGit} from './che-git';
import {CheSvn} from './che-svn';
import {CheProjectType} from './che-project-type';
import {CheTypeResolver} from './project/che-type-resolver';
import {CheJsonRpcWsagentApi} from './json-rpc/che-json-rpc-wsagent-api';
import {WebsocketClient} from './json-rpc/websocket-client';

export interface IWorkspaceAgentData {
  path: string;
  websocket: string;
  clientId: string;
  machineToken: string;
}

/**
 * This class is handling the call to wsagent API.
 * @author Ann Shumilova
 */
export class CheWorkspaceAgent {

  private $resource: ng.resource.IResourceService;
  private git: CheGit;
  private svn: CheSvn;
  private project: CheProject;
  private projectType: CheProjectType;
  private workspaceAgentData: IWorkspaceAgentData;
  private typeResolver: CheTypeResolver;
  private wsagentApi: CheJsonRpcWsagentApi;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService,
              $q: ng.IQService,
              $websocket: any,
              workspaceAgentData: IWorkspaceAgentData) {
    this.$resource = $resource;
    this.workspaceAgentData = workspaceAgentData;

    this.project = new CheProject($resource, $q, this.workspaceAgentData.path, this.workspaceAgentData.machineToken);
    this.git = new CheGit($resource, this.workspaceAgentData.path);
    this.svn = new CheSvn($resource, this.workspaceAgentData.path);
    this.projectType = new CheProjectType($resource, $q, this.workspaceAgentData.path);
    this.typeResolver = new CheTypeResolver($q, this.project, this.projectType);
    this.wsagentApi = new CheJsonRpcWsagentApi(new WebsocketClient($websocket, $q));
    if (this.workspaceAgentData.clientId) {
      this.wsagentApi.connect(this.workspaceAgentData.websocket, this.workspaceAgentData.clientId);
    }
  }

  getProject(): CheProject {
    return this.project;
  }

  getGit(): CheGit {
    return this.git;
  }

  getProjectType(): CheProjectType {
    return this.projectType;
  }

  getProjectTypeResolver(): CheTypeResolver {
    return this.typeResolver;
  }

  getSvn(): CheSvn {
    return this.svn;
  }

  getWsAgentApi(): CheJsonRpcWsagentApi {
    return this.wsagentApi;
  }
}
