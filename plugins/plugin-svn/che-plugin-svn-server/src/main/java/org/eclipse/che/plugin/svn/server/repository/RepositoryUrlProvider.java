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
package org.eclipse.che.plugin.svn.server.repository;

import org.eclipse.che.plugin.svn.server.SubversionException;

/**
 * Detects repository url based on location.
 *
 * @author Anatolii Bazko
 */
public interface RepositoryUrlProvider {

    /**
     * Detects repository url based on location.
     *
     * @param projectPath
     *      the absolute project path
     * @return the repository url of the given project
     * @throws SubversionException
     *      if any error occurs
     */
    String getRepositoryUrl(String projectPath) throws SubversionException;
}
