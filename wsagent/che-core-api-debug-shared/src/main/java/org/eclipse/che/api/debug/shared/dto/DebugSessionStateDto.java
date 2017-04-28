/*******************************************************************************
 * Copyright (c) 2017 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.debug.shared.dto;

import org.eclipse.che.api.debug.shared.model.DebugSessionState;
import org.eclipse.che.dto.shared.DTO;

/**
 * Debug session state DTO.
 * 
 * @author Bartlomiej Laczkowski
 */
@DTO
public interface DebugSessionStateDto extends DebugSessionState {
    
    void setDebuggerType(String debuggerType);
    
    DebugSessionStateDto withDebuggerType(String debuggerType);
    
    @Override
    DebugSessionDto getDebugSession();

    void setDebugSession(DebugSessionDto debugSession);

    DebugSessionStateDto withDebugSession(DebugSessionDto debugSession);
    
    @Override
    LocationDto getLocation();

    void setLocation(LocationDto location);

    DebugSessionStateDto withLocation(LocationDto location);
    
}
