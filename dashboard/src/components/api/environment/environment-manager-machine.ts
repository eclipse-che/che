/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
export interface IEnvironmentManagerMachine extends che.IEnvironmentMachine {
  name: string;
  recipe?: any;
  runtime?: any;
  servers?: {
    [serverRef: string]: IEnvironmentManagerMachineServer
  };
}

export interface IEnvironmentManagerMachineServer extends che.IEnvironmentMachineServer {
  userScope?: boolean; // indicates a server added by user
  runtime?: che.IWorkspaceRuntimeMachineServer;
}
