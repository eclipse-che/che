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
package org.eclipse.che.plugin.svn.server.repository;

import java.io.File;
import java.io.IOException;

import org.eclipse.che.plugin.svn.server.upstream.CommandLineResult;
import org.eclipse.che.plugin.svn.server.upstream.UpstreamUtils;
import org.eclipse.che.plugin.svn.server.utils.InfoUtils;


public class RepositoryUrlProviderImpl implements RepositoryUrlProvider {

    @Override
    public String getRepositoryUrl(final String projectPath) throws IOException {
        final File projectPathFile = new File(projectPath);
        final CommandLineResult clResult = UpstreamUtils.executeCommandLine(null, "svn", new String[]{"info"},
                                                                            null, -1L, projectPathFile);
        return InfoUtils.getUrl(clResult.getStdout());
    }

}
