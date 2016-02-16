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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitProjectImporter;

/**
 * @author Roman Nikitenko
 */
@Singleton
public class GitHubProjectImporter extends GitProjectImporter {

    @Inject
    public GitHubProjectImporter(GitConnectionFactory gitConnectionFactory) {
        super(gitConnectionFactory);
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
