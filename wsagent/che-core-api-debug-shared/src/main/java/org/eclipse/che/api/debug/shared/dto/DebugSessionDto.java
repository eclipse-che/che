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
package org.eclipse.che.api.debug.shared.dto;

import org.eclipse.che.api.debug.shared.model.DebugSession;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Anatoliy Bazko
 */
@DTO
public interface DebugSessionDto extends DebugSession {
    DebuggerInfoDto getDebuggerInfo();

    void setDebuggerInfo(DebuggerInfoDto debuggerInfo);

    DebugSessionDto withDebuggerInfo(DebuggerInfoDto debuggerInfo);

    String getId();

    void setId(String id);

    DebugSessionDto withId(String id);

    String getType();

    void setType(String type);

    DebugSessionDto withType(String type);
}
