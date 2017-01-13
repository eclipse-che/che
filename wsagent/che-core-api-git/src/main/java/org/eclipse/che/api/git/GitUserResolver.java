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
package org.eclipse.che.api.git;

import org.eclipse.che.api.git.shared.GitUser;

/**
 * Resolves {@link GitUser} for any git related operations.
 *
 * @author Max Shaposhnik
 */
public interface GitUserResolver {

    /**
     * Retrieves user for git operations.
     *
     * @return credentials of current user to execute git operation
     *
     */
    GitUser getUser();

}
