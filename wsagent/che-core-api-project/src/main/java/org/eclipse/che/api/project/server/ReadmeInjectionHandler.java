/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ReadmeInjectionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ReadmeInjectionHandler.class);

    private final ReadmeInjectionVerifier  verifier;
    private final ReadmeInjectionPerformer performer;

    @Inject
    public ReadmeInjectionHandler(ReadmeInjectionVerifier verifier, ReadmeInjectionPerformer performer) {
        this.verifier = verifier;
        this.performer = performer;
    }

    public void handleReadmeInjection(FolderEntry projectFolder) {
        try {
            final boolean rootProject = verifier.isRootProject(projectFolder);
            final boolean readmeNotPresent = verifier.isReadmeNotPresent(projectFolder);
            if (rootProject && readmeNotPresent) {
                performer.injectReadmeTo(projectFolder);
            }
        } catch (ServerException | ForbiddenException | IOException | ConflictException e) {
            LOG.error("Cannot inject README.md file", e);
        }
    }
}
