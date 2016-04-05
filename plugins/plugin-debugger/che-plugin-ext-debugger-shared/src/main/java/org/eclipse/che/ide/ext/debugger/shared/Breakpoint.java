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
package org.eclipse.che.ide.ext.debugger.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Description of debug breakpoint.
 *
 * @author andrew00x
 */
@DTO
public interface Breakpoint {
    Location getLocation();

    void setLocation(Location location);

    Breakpoint withLocation(Location location);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    Breakpoint withEnabled(boolean enabled);

    String getCondition();

    void setCondition(String condition);

    Breakpoint withCondition(String condition);
}