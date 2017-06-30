/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.activity.inject;

import org.eclipse.che.api.workspace.server.activity.WorkspaceActivityManager;
import org.eclipse.che.api.workspace.server.activity.WorkspaceActivityService;

import com.google.inject.AbstractModule;

public class WorkspaceActivityModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WorkspaceActivityService.class);
        bind(WorkspaceActivityManager.class);
    }
}
