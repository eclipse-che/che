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
package org.eclipse.che.api.core.model.machine;

/**
 * Represents saved state of a machine
 *
 * @author gazarenkov
 * @author Yevhenii Voevodin
 */
public interface Snapshot {

    /**
     * Unique identifier of snapshot
     */
    String getId();

    /**
     * Type of the instance implementation, e.g. docker
     */
    String getType();

    /**
     * Creation date of the snapshot
     */
    long getCreationDate();

    boolean isDev();

    /**
     * Description of the snapshot
     */
    String getDescription();

    /**
     * Id of workspace which machines is bound to snapshot
     */
    String getWorkspaceId();

    /**
     * Returns name of bound to this snapshot machine
     */
    String getMachineName();

    /**
     * Returns name of environment which machine belongs to
     */
    String getEnvName();
}
