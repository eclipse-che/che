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

import org.eclipse.che.dto.shared.DTO;

/**
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

}
