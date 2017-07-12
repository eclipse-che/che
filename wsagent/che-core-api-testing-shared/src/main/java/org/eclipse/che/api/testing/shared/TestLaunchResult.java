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
package org.eclipse.che.api.testing.shared;

import org.eclipse.che.dto.shared.DTO;

import javax.validation.constraints.NotNull;

/**
 * Describes test position in document
 */
@DTO
public interface TestLaunchResult {

    /**
     * @return {@code true} if tests were launched successfully otherwise returns false
     */
    @NotNull
    boolean isSuccess();

    void setSuccess(boolean isSuccess);

    TestLaunchResult withSuccess(boolean isSuccess);

    /**
     * @return port for connecting to the debugger if Debug Mode is on
     */
    @NotNull
    int getDebugPort();

    void setDebugPort(int port);

    TestLaunchResult withDebugPort(int port);
}
