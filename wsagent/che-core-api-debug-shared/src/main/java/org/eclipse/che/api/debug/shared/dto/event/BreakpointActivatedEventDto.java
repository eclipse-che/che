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
package org.eclipse.che.api.debug.shared.dto.event;

import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.dto.shared.DTO;

/**
 * Event will be generated when breakpoint become active.
 *
 * @author Anatoliy Bazko
 */
@DTO
public interface BreakpointActivatedEventDto extends DebuggerEventDto {
    TYPE getType();

    void setType(TYPE type);

    BreakpointActivatedEventDto withType(TYPE type);

    BreakpointDto getBreakpoint();

    void setBreakpoint(BreakpointDto breakpoint);

    BreakpointActivatedEventDto withBreakpoint(BreakpointDto breakpoint);
}
