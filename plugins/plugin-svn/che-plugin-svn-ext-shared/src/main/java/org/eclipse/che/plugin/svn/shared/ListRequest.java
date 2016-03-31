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

import javax.validation.constraints.NotNull;

@DTO
public interface ListRequest {

    /**
     * @return the project path the request is associated with.
     */
    String getProjectPath();

    /**
     * @param projectPath
     */
    void setProjectPath(@NotNull final String projectPath);

    /**
     * @param projectPath project path
     */
    ListRequest withProjectPath(@NotNull final String projectPath);


    /**
     * @return target URL to list
     */
    String getTarget();

    /**
     * @param target
     */
    void setTarget(@NotNull final String target);

    /**
     * @param target target to list
     */
    ListRequest withTarget(@NotNull final String target);

}
