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
package org.eclipse.che.ide.ext.github.client;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.ext.git.ssh.client.GitSshKeyUploaderRegistry;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Extension adds GitHub support to the IDE Application.
 *
 * @author Andrey Plotnikov
 */
@Singleton
@Extension(title = "GitHub", version = "3.0.0")
public class GitHubExtension {

    public static final String GITHUB_HOST = "github.com";

    @Inject
    public GitHubExtension(GitSshKeyUploaderRegistry registry, GitHubSshKeyUploader gitHubSshKeyProvider) {
        registry.registerUploader(GITHUB_HOST, gitHubSshKeyProvider);
    }
}
