/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
import che = _che;

declare namespace _che {

  export interface IRootScopeService extends ng.IRootScopeService {
    hideLoader: boolean;
    showIDE: boolean;
    wantTokeepLoader: boolean;
  }

  export namespace route {

    export interface IRouteParamsService extends ng.route.IRouteParamsService {
      action: string;
      ideParams: string | string[];
      namespace: string;
      showLogs: string;
      workspaceName: string;
    }

  }

}
