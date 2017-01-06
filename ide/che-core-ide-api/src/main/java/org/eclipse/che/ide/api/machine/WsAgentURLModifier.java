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
package org.eclipse.che.ide.api.machine;


/**
 * Modify the URL to the workspace agent.
 * <p>Note that for che assembly it's return the source url.
 *
 * @author Anton Korneta
 */
public interface WsAgentURLModifier {

    /**
     * Initializes modifier during the start of the workspace
     *
     * @param devMachine
     *         runtime information of dev machine instance, such as link
     */
    void initialize(DevMachine devMachine);

    /**
     * Change source url in accordance with the requirements of the assembly
     *
     * @param agentUrl
     *         any url to the workspace agent
     * @return modified url
     */
    String modify(String agentUrl);
}
