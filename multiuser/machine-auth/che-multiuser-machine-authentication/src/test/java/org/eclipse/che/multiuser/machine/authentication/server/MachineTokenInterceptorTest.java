/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.spi.ConstructorBinding;
import java.lang.reflect.Method;
import javax.persistence.EntityManagerFactory;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.WorkspaceSharedPool;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.core.db.DBInitializer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
@Listeners(MockitoTestNGListener.class)
public class MachineTokenInterceptorTest {
  private static final String USER_ID = "user12";
  private static final String USER_NAME = "username";

  @Mock private MachineTokenRegistry tokenRegistry;
  @Mock private WorkspaceImpl workspaceImpl;

  private WorkspaceRuntimes workspaceRuntimes;

  private Injector injector;

  @BeforeMethod
  public void setUp() throws Throwable {

    Module module =
        new AbstractModule() {
          public void configure() {
            // Bind manager and his dep-s. To bind interceptor, guice must create intercepted class
            // by himself.
            bind(WorkspaceDao.class).toInstance(mock(WorkspaceDao.class));
            bind(EventService.class).toInstance(mock(EventService.class));
            bind(EntityManagerFactory.class).toInstance(mock(EntityManagerFactory.class));
            bind(DBInitializer.class).toInstance(mock(DBInitializer.class));
            bind(WorkspaceSharedPool.class)
                .toInstance(new WorkspaceSharedPool("cached", null, null));

            bindConstant().annotatedWith(Names.named("che.api")).to("localhost");

            bind(MachineTokenRegistry.class).toInstance(tokenRegistry);

            Multibinder.newSetBinder(binder(), RuntimeInfrastructure.class);

            MapBinder.newMapBinder(binder(), String.class, InternalEnvironmentFactory.class);

            bind(WorkspaceRuntimes.class);

            // Main injection
            install(new MachineAuthModule());

            // To prevent real methods of manager calling
            bindInterceptor(
                subclassesOf(WorkspaceRuntimes.class), names("startAsync"), invocation -> null);
          }
        };

    injector = Guice.createInjector(module);
    workspaceRuntimes = injector.getInstance(WorkspaceRuntimes.class);
    EnvironmentContext.setCurrent(
        new EnvironmentContext() {
          @Override
          public Subject getSubject() {
            return new SubjectImpl(USER_NAME, USER_ID, "token", false);
          }
        });
  }

  @Test
  public void checkAllInterceptedMethodsArePresent() throws Throwable {
    ConstructorBinding<?> interceptedBinding =
        (ConstructorBinding<?>) injector.getBinding(WorkspaceRuntimes.class);

    for (Method method : interceptedBinding.getMethodInterceptors().keySet()) {
      workspaceRuntimes.getClass().getMethod(method.getName(), method.getParameterTypes());
    }
  }

  @Test
  public void shouldGenerateTokenOnWorkspaceStart() throws Throwable {
    final String workspaceId = "testWs123";
    when(workspaceImpl.getId()).thenReturn(workspaceId);

    workspaceRuntimes.startAsync(workspaceImpl, null, null);

    verify(tokenRegistry).generateToken(eq(USER_ID), eq(workspaceId));
  }
}
