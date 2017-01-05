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

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author andrew00x
 */
@DTO
public interface BreakpointDto extends Breakpoint {
    LocationDto getLocation();

    void setLocation(LocationDto location);

    BreakpointDto withLocation(LocationDto location);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    BreakpointDto withEnabled(boolean enabled);

    String getCondition();

    void setCondition(String condition);

    BreakpointDto withCondition(String condition);
}
