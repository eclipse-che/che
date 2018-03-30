/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {
  CheWorkspaceJsonRpcClientService,
  IWorkspaceMasterApi
} from '../workspace/che-workspace-json-rpc-client.service';

/**
 * This class manages the api connection through JSON RPC.
 *
 * @author Ann Shumilova
 */
export class CheJsonRpcApi {

  static $inject = ['cheWorkspaceJsonRpcClient'];

  private jsonRpcApiConnection: Map<string, any>;
  private cheWorkspaceJsonRpcClient: CheWorkspaceJsonRpcClientService;

  /**
   * Default constructor that is using resource
   */
  constructor(cheWorkspaceJsonRpcClient: CheWorkspaceJsonRpcClientService) {
    this.jsonRpcApiConnection = new Map<string, IWorkspaceMasterApi>();
    this.cheWorkspaceJsonRpcClient = cheWorkspaceJsonRpcClient;
  }

  getJsonRpcMasterApi(entryPoint: string): IWorkspaceMasterApi {
   if (this.jsonRpcApiConnection.has(entryPoint)) {
     return this.jsonRpcApiConnection.get(entryPoint);
   } else {
     const cheJsonRpcMasterApi = this.cheWorkspaceJsonRpcClient.getMasterApiClient(entryPoint);
     cheJsonRpcMasterApi.connect(entryPoint);
     this.jsonRpcApiConnection.set(entryPoint, cheJsonRpcMasterApi);
     return cheJsonRpcMasterApi;
   }
  }
}
