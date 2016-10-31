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
package org.eclipse.che.api.agent.shared.model;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * A pair of id and version of the agent.
 * Version part is not mandatory.
 *
 * @author Anatolii Bazko
 */
public interface AgentKey {
    /**
     * @return the id of the agent
     */
    String getId();

    /**
     * @return the version of the agent
     */
    @Nullable
    String getVersion();
}
