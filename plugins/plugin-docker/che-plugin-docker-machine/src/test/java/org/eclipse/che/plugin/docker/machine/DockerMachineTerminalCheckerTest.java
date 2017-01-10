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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.plugin.docker.machine.ext.DockerMachineTerminalChecker;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


/**
 * @author Max Shaposhnik
 *
 */

@Listeners(value = {MockitoTestNGListener.class})
public class DockerMachineTerminalCheckerTest {

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldThrowRuntimeExceptionIfNoTerminalArchivePresent() {
        DockerMachineTerminalChecker checker = new DockerMachineTerminalChecker("/no/such/path");

        checker.start();
    }
}
