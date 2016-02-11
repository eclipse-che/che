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
package org.eclipse.che.plugin.docker.client;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.docker.client.json.Version;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class DockerVersionVerifierTest {

    @Mock
    private DockerConnector       dockerConnector;

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Unsupported docker version x.x.x")
    public void shouldThrowServerExceptionWhenDockerVersionIsIncompatible() throws Exception {
        //mock docker version
        Version version = new Version();
        version.setVersion("x.x.x");
        when(dockerConnector.getVersion()).thenReturn(version);
        //prepare verifies
        DockerVersionVerifier verifier = new DockerVersionVerifier(dockerConnector, new String[]{"1.6.0"});

        verifier.checkCompatibility();
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Impossible to get docker version")
    public void shouldThrowIOExceptionWhenVersionJsonParsing() throws Exception {
        when(dockerConnector.getVersion()).thenThrow(new IOException());
        DockerVersionVerifier verifier = new DockerVersionVerifier(dockerConnector, new String[]{"1.6.0"});
        verifier.checkCompatibility();
    }

    @Test
    public void supportedVersionTest() throws Exception {
        Version version = new Version();
        version.setVersion("1.6.0");
        when(dockerConnector.getVersion()).thenReturn(version);
        DockerVersionVerifier verifier = new DockerVersionVerifier(dockerConnector, new String[]{"1.6.0"});
        verifier.checkCompatibility();
    }
}
