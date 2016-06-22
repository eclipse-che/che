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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DockerRegistryChecker}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerRegistryCheckerTest {

    private static final String URL      = "https://index.docker.io/v1/";
    private static final int    TIME_OUT = 30 * 1000;

    @Mock
    private HttpJsonRequestFactory requestFactory;
    @Mock
    private HttpJsonRequest        jsonRequest;
    @Mock
    private HttpJsonResponse       httpJsonResponse;

    @Test
    public void registryShouldBeAvailable() throws ApiException, IOException {
        when(requestFactory.fromUrl("http://" + URL)).thenReturn(jsonRequest);
        when(jsonRequest.setTimeout(TIME_OUT)).thenReturn(jsonRequest);
        when(jsonRequest.request()).thenReturn(httpJsonResponse);
        DockerRegistryChecker dockerRegistryChecker = new DockerRegistryChecker(requestFactory, URL, true);

        dockerRegistryChecker.checkRegistryIsAvailable();

        verify(requestFactory).fromUrl(anyString());
        verify(jsonRequest).setTimeout(TIME_OUT);
        verify(jsonRequest).request();
        verify(httpJsonResponse).getResponseCode();
    }

    @Test
    public void registryShouldNotBeAvailableIfPropertySnapshotUseRegistryIsNull() {
        DockerRegistryChecker dockerRegistryChecker = new DockerRegistryChecker(requestFactory, URL, null);

        dockerRegistryChecker.checkRegistryIsAvailable();

        verify(requestFactory, never()).fromUrl(anyString());
    }

    @Test
    public void registryShouldNotBeAvailableIfPropertySnapshotUseRegistryIsFalse() {
        DockerRegistryChecker dockerRegistryChecker = new DockerRegistryChecker(requestFactory, URL, false);

        dockerRegistryChecker.checkRegistryIsAvailable();

        verify(requestFactory, never()).fromUrl(anyString());
    }

    @Test
    public void registryShouldNotBeAvailableIfRegistryUrlIsEmpty() {
        DockerRegistryChecker dockerRegistryChecker = new DockerRegistryChecker(requestFactory, "", true);

        dockerRegistryChecker.checkRegistryIsAvailable();

        verify(requestFactory, never()).fromUrl(anyString());
    }

    @Test
    public void registryShouldNotBeAvailableIfRegistryUrlIsNull() {
        DockerRegistryChecker dockerRegistryChecker = new DockerRegistryChecker(requestFactory, null, true);

        dockerRegistryChecker.checkRegistryIsAvailable();

        verify(requestFactory, never()).fromUrl(anyString());
    }
}

