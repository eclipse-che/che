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

import javax.validation.constraints.NotNull;

@DTO
public interface InfoRequest {

    /**************************************************************************
     *
     *  Project path
     *
     **************************************************************************/

    String getProjectPath();

    void setProjectPath(@NotNull final String projectPath);

    InfoRequest withProjectPath(@NotNull final String projectPath);

    /**************************************************************************
     *
     *  Target
     *
     **************************************************************************/

    String getTarget();

    void setTarget(@NotNull final String target);

    InfoRequest withTarget(@NotNull final String target);

    /**************************************************************************
     *
     *  Revision
     *
     **************************************************************************/

    String getRevision();

    void setRevision(@NotNull final String revision);

    InfoRequest withRevision(@NotNull final String revision);

    /**************************************************************************
     *
     *  Children
     *
     **************************************************************************/

    boolean getChildren();

    void setChildren(@NotNull final boolean children);

    InfoRequest withChildren(@NotNull final boolean children);

    /** @return user name for authentication */
    String getUsername();

    /** Set user name for authentication. */
    void setUsername(@Nullable final String username);

    /** @return {@link CheckoutRequest} with specified user name for authentication */
    InfoRequest withUsername(@Nullable final String username);

    /** @return password for authentication */
    String getPassword();

    /** Set password for authentication. */
    void setPassword(@Nullable final String password);

    /** @return {@link CheckoutRequest} with specified password for authentication */
    InfoRequest withPassword(@Nullable final String password);

}
