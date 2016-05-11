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
package org.eclipse.che.ide.api.debugger.shared.dto.event;

import org.eclipse.che.ide.api.debugger.shared.dto.BreakpointDto;
import org.eclipse.che.ide.api.debugger.shared.model.event.DebuggerEvent;
import org.eclipse.che.dto.shared.DTO;

/**
 * Event will be generated when breakpoint become active.
 *
 * @author Anatoliy Bazko
 */
@DTO
public interface BreakpointActivatedEventDto extends DebuggerEventDto {
    DebuggerEvent.TYPE getType();

    void setType(DebuggerEvent.TYPE type);

    BreakpointActivatedEventDto withType(DebuggerEvent.TYPE type);

    BreakpointDto getBreakpoint();

    void setBreakpoint(BreakpointDto breakpoint);

    BreakpointActivatedEventDto withBreakpoint(BreakpointDto breakpoint);
}
