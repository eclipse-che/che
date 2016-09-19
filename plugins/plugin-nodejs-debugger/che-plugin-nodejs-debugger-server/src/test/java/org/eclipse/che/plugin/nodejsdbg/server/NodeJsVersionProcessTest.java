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

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Anatolii Bazko
 */
public class NodeJsVersionProcessTest {

    @Test
    public void testGetNodeJsVersion() throws Exception {
        NodeJsVersionProcess nodeJsVersionProcess = new NodeJsVersionProcess();

        assertNotNull(nodeJsVersionProcess.getVersion());
        assertNotNull(nodeJsVersionProcess.getName());
    }
}
