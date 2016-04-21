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
package org.eclipse.che.plugin.docker.machine.local.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerImage;
import org.eclipse.che.plugin.docker.client.Dockerfile;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

@Listeners(MockitoTestNGListener.class)
public class EnableOfflineDockerMachineBuildInterceptorTest {
    @Mock
    private DockerConnector dockerConnector;

    @Mock
    private MethodInvocation methodInvocation;

    @Mock
    private LineConsumer lineConsumer;

    @Mock
    private Dockerfile dockerfile;

    @Mock
    private DockerImage dockerImage;

    @InjectMocks
    private EnableOfflineDockerMachineBuildInterceptor interceptor;

    @Test
    public void shouldProceedInterceptedMethodIfForcePullIsDisabled() throws Throwable {
        final Object[] arguments = {dockerfile, lineConsumer, "string", Boolean.FALSE, 0L, 0L};
        when(methodInvocation.getArguments()).thenReturn(arguments);


        interceptor.invoke(methodInvocation);


        assertFalse((Boolean)arguments[3]);
        verify(methodInvocation).proceed();
    }

    @Test
    public void shouldPullDockerImageIfForcePullIsEnabled() throws Throwable {
        final Object[] arguments = {dockerfile, lineConsumer, "string", Boolean.TRUE, 0L, 0L};
        when(methodInvocation.getArguments()).thenReturn(arguments);
        when(dockerfile.getImages()).thenReturn(Collections.singletonList(dockerImage));
        final String tag = "latest";
        final String repo = "my_repo/my_image";
        when(dockerImage.getFrom()).thenReturn(repo + ":" + tag);


        interceptor.invoke(methodInvocation);


        assertFalse((Boolean)arguments[3]);
        verify(methodInvocation).proceed();
        verify(dockerfile).getImages();
        verify(dockerConnector).pull(eq(repo), eq(tag), eq(null), any(ProgressMonitor.class));
    }

    @Test(dataProvider = "throwableProvider")
    public void shouldIgnoreExceptionsOnDockerImagePullingIfForcePullIsEnabled(Throwable throwable) throws Throwable {
        final Object[] arguments = {dockerfile, lineConsumer, "string", Boolean.TRUE, 0L, 0L};
        when(methodInvocation.getArguments()).thenReturn(arguments);
        when(dockerfile.getImages()).thenReturn(Collections.singletonList(dockerImage));
        final String tag = "latest";
        final String repo = "my_repo/my_image";
        when(dockerImage.getFrom()).thenReturn(repo + ":" + tag);
        doThrow(throwable).when(dockerConnector).pull(anyString(), anyString(), anyString(), any(ProgressMonitor.class));


        interceptor.invoke(methodInvocation);


        assertFalse((Boolean)arguments[3]);
        verify(methodInvocation).proceed();
    }

    @Test
    public void shouldPullLatestIfNoTagFoundInDockerfile() throws Throwable {
        final Object[] arguments = {dockerfile, lineConsumer, "string", Boolean.TRUE, 0L, 0L};
        when(methodInvocation.getArguments()).thenReturn(arguments);
        when(dockerfile.getImages()).thenReturn(Collections.singletonList(dockerImage));
        final String repo = "my_repo/my_image";
        when(dockerImage.getFrom()).thenReturn(repo);


        interceptor.invoke(methodInvocation);


        assertFalse((Boolean)arguments[3]);
        verify(methodInvocation).proceed();
        verify(dockerfile).getImages();
        verify(dockerConnector).pull(eq(repo), eq("latest"), eq(null), any(ProgressMonitor.class));
    }

    @DataProvider(name = "throwableProvider")
    public static Object[][] throwableProvider() {
        return new Object[][] {{new IOException("test_exception")},
                               {new InterruptedException("test_exception")}};
    }
}
