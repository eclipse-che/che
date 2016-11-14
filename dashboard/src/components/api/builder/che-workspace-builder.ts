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
'use strict';


/**
 * This class is providing a builder for Workspace
 * @author Florent Benoit
 */
export class CheWorkspaceBuilder {
  workspace: che.IWorkspace;

  constructor() {
    this.workspace = {
      temporary: false,
      config: {
        projects: []
      }
    };

  }

  withName(name: string) {
    this.workspace.config.name = name;
    return this;
  }

  withId(id: string) {
    this.workspace.id = id;
    return this;
  }

  withTemporary(temporary: boolean) {
    this.workspace.temporary = temporary;
    return this;
  }

  withRuntime(runtime: any) {
    this.workspace.runtime = runtime;
    return this;
  }

  build() {
    return this.workspace;
  }

}

