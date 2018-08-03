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
