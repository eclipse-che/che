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
package org.eclipse.che.plugin.svn.shared;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO object for {@code svn list} requests.
 *
 * @author Anatolii Bazko
 */
@DTO
public interface ListRequest {

    /**
     * Returns the project path.
     */
    String getProjectPath();

    void setProjectPath(String projectPath);

    ListRequest withProjectPath(String projectPath);

    /**
     * Returns the target path to browse.
     */
    String getTargetPath();

    void setTargetPath(String targetPath);

    ListRequest withTargetPath(String targetPath);


    /**************************************************************************
     * Credentials
     **************************************************************************/

    String getUsername();

    void setUsername(@Nullable final String username);

    ListRequest withUsername(@Nullable final String username);

    String getPassword();

    void setPassword(@Nullable final String password);

    ListRequest withPassword(@Nullable final String password);
}
