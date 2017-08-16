/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ServerException;

/**
 * Version control system status provider.
 *
 * @author Igor Vinokur
 */
public interface VcsStatusProvider {

    /**
     * Returns name of the version control system.
     */
    String getVcsName();

    /**
     * Get vcs status of the given file.
     *
     * @param path
     *         path to the given file
     */
    VcsStatus getStatus(String path) throws ServerException;

    enum VcsStatus {
        ADDED,
        MODIFIED,
        NOT_MODIFIED,
        UNTRACKED
    }
}
