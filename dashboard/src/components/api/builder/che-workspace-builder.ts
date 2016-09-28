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

  constructor() {
    this.workspace = {
      config: {
        projects: [],
        temporary: false
      }
    };

  }

  withName(name) {
    this.workspace.config.name = name;
    return this;
  }

  withId(id) {
    this.workspace.id = id;
    return this;
  }

  withTemporary(temporary) {
    this.workspace.config.temporary = temporary;
    return this;
  }

  withRuntime(runtime) {
    this.workspace.runtime = runtime;
    return this;
  }

  build() {
    return this.workspace;
  }


}

