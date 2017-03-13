/*******************************************************************************
 * Copyright (c) 2017 RedHat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc. - initial commit
 *******************************************************************************/
package org.eclipse.che.api.testing.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Provide the output of Testing tools.
 * 
 * @author David Festal
 */
@DTO
public interface TestingOutput {

    enum LineType {
        SESSION_START,
        DETAIL,
        SUCCESS,
        ERROR,
        FAILURE,
        SESSION_END,
    }

    /**
     * Output line
     * @return
     */
    String getOutput();

    /**
     * for a success notification line will be State.SUCCESS
     * for a failure notification line will be State.FAILURE
     * for an error notification will be State.ERROR
     * for all other lines will be State.DETAIL
     * @return
     */
    LineType getState();

}
