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
package org.eclipse.che.api.debug.shared.model;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public interface ThreadDump {

    /**
     * Returns thread name.
     */
    String getName();

    /**
     * Returns thread group name.
     */
    @Nullable
    String getGroupName();

    /**
     * Returns list of frames of the thread.
     */
    List<? extends StackFrameDump> getFrames();

    /**
     * Returns thread state.
     */
    ThreadStatus getStatus();

    /**
     * Indicates if thread is suspended.
     */
    boolean isSuspended();
}
