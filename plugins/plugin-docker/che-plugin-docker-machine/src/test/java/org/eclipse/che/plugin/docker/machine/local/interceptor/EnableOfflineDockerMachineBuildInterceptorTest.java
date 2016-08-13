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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.spi.ConstructorBinding;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.machine.server.util.RecipeRetriever;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.machine.DockerInstanceProvider;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.DockerMachineModule;
import org.eclipse.che.plugin.docker.machine.local.node.provider.LocalWorkspaceFolderPathProvider;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class EnableOfflineDockerMachineBuildInterceptorTest {
    private static final String REPO  = "some/image";
    private static final String TAG   = "tag1";
    private static final String IMAGE = REPO + ":" + TAG;
    private static final String MACHINE_IMAGE_NAME = "machineImageName";

    @Mock
    private DockerConnector                               dockerConnector;
    @Mock
    private UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
    @Mock
    private DockerMachineFactory                          dockerMachineFactory;
    @Mock
    private WorkspaceManager                              workspaceManager;
    @Mock
    private MethodInvocation                              methodInvocation;
    @Mock
    private MachineConfig                                 machineConfig;
    @Mock
    private ProgressMonitor                               progressMonitor;
    @Mock
    private RecipeRetriever                               recipeRetriever;
    @Mock
    private Recipe                                        recipe;
    @Mock
    private Supplier<Boolean>                             doForcePullOnBuildFlagProvider;


    private DockerInstanceProvider                     dockerInstanceProvider;
    private Injector                                   injector;
    private EnableOfflineDockerMachineBuildInterceptor interceptor;

    @BeforeMethod
    private void setup() throws Exception {
        when(dockerCredentials.getCredentials()).thenReturn(null);
        when(recipeRetriever.getRecipe(any(MachineConfig.class))).thenReturn(recipe);
        when(recipe.getScript()).thenReturn("FROM " + IMAGE);
        when(doForcePullOnBuildFlagProvider.get()).thenReturn(true);

        injector = Guice.createInjector(new TestModule());
        dockerInstanceProvider = injector.getInstance(DockerInstanceProvider.class);
        interceptor = injector.getInstance(EnableOfflineDockerMachineBuildInterceptor.class);
    }

    @Test
    public void checkAllInterceptedMethodsArePresent() throws Throwable {
        ConstructorBinding<?> interceptedBinding
                = (ConstructorBinding<?>)injector.getBinding(EnableOfflineDockerMachineBuildInterceptor.class);

        for (Method method : interceptedBinding.getMethodInterceptors().keySet()) {
            dockerInstanceProvider.getClass().getMethod(method.getName(), method.getParameterTypes());
        }
    }

    @Test
    public void shouldProceedInterceptedMethodIfForcePullIsDisabled() throws Throwable {
        when(doForcePullOnBuildFlagProvider.get()).thenReturn(true);

        injector = Guice.createInjector(new TestModule());
        dockerInstanceProvider = injector.getInstance(DockerInstanceProvider.class);
        interceptor = injector.getInstance(EnableOfflineDockerMachineBuildInterceptor.class);

        final Object[] arguments = {machineConfig, MACHINE_IMAGE_NAME, false, progressMonitor};
        when(methodInvocation.getArguments()).thenReturn(arguments);


        interceptor.invoke(methodInvocation);


        verify(methodInvocation).proceed();
        assertEquals(methodInvocation.getArguments(), new Object[] {machineConfig, MACHINE_IMAGE_NAME, false, progressMonitor});
    }

    @Test
    public void shouldPullDockerImageIfForcePullIsEnabled() throws Throwable {
        final Object[] arguments = {machineConfig, MACHINE_IMAGE_NAME, true, progressMonitor};
        when(methodInvocation.getArguments()).thenReturn(arguments);
        final PullParams pullParams = PullParams.create(REPO)
                                                .withTag(TAG)
                                                .withAuthConfigs(null);


        interceptor.invoke(methodInvocation);


        verify(methodInvocation).proceed();
        assertEquals(methodInvocation.getArguments(), new Object[] {machineConfig, MACHINE_IMAGE_NAME, false, progressMonitor});
        verify(dockerConnector).pull(eq(pullParams), any(ProgressMonitor.class));
    }

    @Test
    public void shouldIgnoreExceptionsOnDockerImagePulling() throws Throwable {
        final Object[] arguments = {machineConfig, MACHINE_IMAGE_NAME, true, progressMonitor};
        when(methodInvocation.getArguments()).thenReturn(arguments);
        doThrow(new IOException()).when(dockerConnector).pull(any(PullParams.class), any(ProgressMonitor.class));
        final PullParams pullParams = PullParams.create(REPO)
                                                .withTag(TAG)
                                                .withAuthConfigs(null);


        interceptor.invoke(methodInvocation);


        verify(methodInvocation).proceed();
        assertEquals(methodInvocation.getArguments(), new Object[] {machineConfig, MACHINE_IMAGE_NAME, false, progressMonitor});
        verify(dockerConnector).pull(eq(pullParams), any(ProgressMonitor.class));
    }

    @Test
    public void shouldPullLatestIfNoTagFoundInDockerfile() throws Throwable {
        final Object[] arguments = {machineConfig, MACHINE_IMAGE_NAME, true, progressMonitor};
        when(methodInvocation.getArguments()).thenReturn(arguments);
        when(recipe.getScript()).thenReturn("FROM " + REPO);
        final PullParams pullParams = PullParams.create(REPO)
                                                .withTag("latest")
                                                .withAuthConfigs(null);


        interceptor.invoke(methodInvocation);


        verify(methodInvocation).proceed();
        assertEquals(methodInvocation.getArguments(), new Object[] {machineConfig, MACHINE_IMAGE_NAME, false, progressMonitor});
        verify(dockerConnector).pull(eq(pullParams), any(ProgressMonitor.class));
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(DockerInstanceProvider.class);
            bind(DockerConnector.class).toInstance(dockerConnector);
            bind(UserSpecificDockerRegistryCredentialsProvider.class).toInstance(dockerCredentials);
            bind(WorkspaceFolderPathProvider.class).to(LocalWorkspaceFolderPathProvider.class);
            bind(DockerMachineFactory.class).toInstance(dockerMachineFactory);
            bind(WorkspaceManager.class).toInstance(workspaceManager);
            bind(RecipeRetriever.class).toInstance(recipeRetriever);
            bind(RecipeDownloader.class).toInstance(mock(RecipeDownloader.class));

            bindConstant().annotatedWith(Names.named("machine.docker.privilege_mode")).to(false);
            bindConstant().annotatedWith(Names.named("machine.docker.pull_image")).to(true);
            bindConstant().annotatedWith(Names.named("machine.docker.snapshot_use_registry")).to(doForcePullOnBuildFlagProvider.get());
            bindConstant().annotatedWith(Names.named("machine.docker.memory_swap_multiplier")).to(1.0);
            bindConstant().annotatedWith(Names.named("che.machine.projects.internal.storage")).to("/tmp");
            bindConstant().annotatedWith(Names.named("machine.docker.machine_extra_hosts")).to("");
            bindConstant().annotatedWith(Names.named("che.workspace.storage")).to("/tmp");
            install(new DockerMachineModule());

            install(new AllowOfflineMachineCreationModule());
        }
    }
}
