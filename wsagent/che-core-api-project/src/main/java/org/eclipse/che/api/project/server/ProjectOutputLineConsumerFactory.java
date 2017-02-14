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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.importer.ProjectImportOutputWSLineConsumer;

/**
 * {@link LineConsumerFactory} dedicated to project related operations long output
 * extended standard factory with setProjectName method to make it possible
 * change it runtime inside ProjectManager
 *
 * @author gazarenkov
 */
public class ProjectOutputLineConsumerFactory implements LineConsumerFactory {

    private       String projectName;
    private final String workspaceId;
    private final int    delay;

    public ProjectOutputLineConsumerFactory(String projectName, String workspaceId, int delay) {
        this.projectName = projectName;
        this.workspaceId = workspaceId;
        this.delay = delay;
    }

    public ProjectOutputLineConsumerFactory(String workspaceId, int delay) {
        this(null, workspaceId, delay);
    }

    public ProjectOutputLineConsumerFactory setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    @Override
    public LineConsumer newLineConsumer() {
        return new ProjectImportOutputWSLineConsumer(projectName, workspaceId, delay);
    }
}
