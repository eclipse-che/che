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

/**
 * @author Anatolii Bazko
 */
public interface AgentKey {
    /**
     * @return fqn of the agent
     */
    String getFqn();

    /**
     * @return the version of the agent
     */
    String getVersion();
}
