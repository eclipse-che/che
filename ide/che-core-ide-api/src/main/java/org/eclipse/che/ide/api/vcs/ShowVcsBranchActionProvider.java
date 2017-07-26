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
package org.eclipse.che.ide.api.vcs;

import org.eclipse.che.ide.api.resources.Project;

/**
 * Provider that open's VCS branches dialog.
 *
 * @author Igor Vinokur
 */
public interface ShowVcsBranchActionProvider {
    /**
     * Returns name of the version control system.
     */
    String getVcsName();

    /**
     * Open VCS branches dialog.
     *
     * @param project
     *         related project
     */
    void show(Project project);
}
