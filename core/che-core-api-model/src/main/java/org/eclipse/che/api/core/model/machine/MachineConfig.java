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
package org.eclipse.che.api.core.model.machine;

/**
 * @author gazarenkov
 */
public interface MachineConfig {

    /**
     * Display name.
     */
    String getName();

    /**
     * From where to create this Machine (Recipe/Snapshot).
     */
    MachineSource getSource();

    /**
     * Is workspace bound to machine or not.
     */
    boolean isDev();

    /**
     * Machine type (i.e. "docker").
     */
    String getType();

    /**
     * Machine limits such as RAM size.
     */
    Limits getLimits();
}
