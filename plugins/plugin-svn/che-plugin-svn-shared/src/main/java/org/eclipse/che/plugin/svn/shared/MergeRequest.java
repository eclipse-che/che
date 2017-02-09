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
package org.eclipse.che.plugin.svn.shared;

import org.eclipse.che.dto.shared.DTO;

import javax.validation.constraints.NotNull;

@DTO
public interface MergeRequest {

    /**************************************************************************
     *
     *  Project path
     *
     **************************************************************************/

    String getProjectPath();

    void setProjectPath(@NotNull final String projectPath);

    MergeRequest withProjectPath(@NotNull final String projectPath);

    /**************************************************************************
     *
     *  Target
     *
     **************************************************************************/

    String getTarget();

    void setTarget(@NotNull final String target);

    MergeRequest withTarget(@NotNull final String target);

    /**************************************************************************
     *
     *  Source
     *
     **************************************************************************/

    String getSourceURL();

    void setSourceURL(@NotNull final String sourceURL);

    MergeRequest withSourceURL(@NotNull final String sourceURL);
}
