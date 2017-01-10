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

import com.google.inject.Singleton;

/**
 * Dummy implementation.
 *
 * @author Anton Korneta
 */
@Singleton
public class CheWsAgentLinksModifier implements WsAgentURLModifier {

    @Override
    public void initialize(DevMachine devMachine) {
    }

    @Override
    public String modify(String agentUrl) {
        return agentUrl;
    }
}
