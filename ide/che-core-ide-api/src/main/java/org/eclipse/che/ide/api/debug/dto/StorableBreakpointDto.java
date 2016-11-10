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
package org.eclipse.che.ide.api.debug.dto;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.ide.api.debug.Breakpoint;

/**
 * {@link DTO} object to preserve breakpoints between user's sessions.
 *
 * @author Anatoliy Bazko
 */
@DTO
public interface StorableBreakpointDto {

    void setActive(boolean active);

    boolean isActive();

    void setLineNumber(int lineNumber);

    int getLineNumber();

    void setType(Breakpoint.Type type);

    Breakpoint.Type getType();

    void setPath(String path);

    String getPath();

    void setFileProjectConfig(ProjectConfigDto projectConfig);

    ProjectConfigDto getFileProjectConfig();
}
