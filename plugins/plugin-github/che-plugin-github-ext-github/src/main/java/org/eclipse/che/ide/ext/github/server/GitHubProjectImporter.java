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
package org.eclipse.che.ide.ext.github.server;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitProjectImporter;
import org.eclipse.che.vfs.impl.fs.LocalPathResolver;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Roman Nikitenko
 */
@Singleton
public class GitHubProjectImporter extends GitProjectImporter {

    @Inject
    public GitHubProjectImporter(GitConnectionFactory gitConnectionFactory,
                                 LocalPathResolver localPathResolver,
                                 EventService eventService) {
        super(gitConnectionFactory, localPathResolver, eventService);
    }

    @Override
    public String getId() {
        return "github";
    }

    @Override
    public String getDescription() {
        return "Import project from github.";
    }
}
