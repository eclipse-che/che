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
package org.eclipse.che.plugin.nodejsdbg.server;

import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;

/**
 * @author Anatolii Bazko
 */
public class NodeJsVersionProcess extends NodeJsProcess {

    private final String version;

    public NodeJsVersionProcess() throws NodeJsDebuggerException {
        super("\n", "-v");
        version = grabOutput().getOutput();
    }

    public String getVersion() { return version; }

    public String getName() { return NODEJS_COMMAND;}
}
