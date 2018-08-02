/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.inject;

import static org.eclipse.che.inject.lifecycle.DestroyErrorHandler.LOG_HANDLER;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.inject.lifecycle.DestroyModule;
import org.eclipse.che.inject.lifecycle.Destroyer;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** @author andrew00x */
public class LifecycleTest {
  Injector injector;

  @BeforeTest
  public void init() {
    injector =
        Guice.createInjector(
            new InitModule(PostConstruct.class),
            new DestroyModule(PreDestroy.class, LOG_HANDLER),
            new MyModule());
  }

  @Test
  public void testInit() {
    TestComponent component = injector.getInstance(TestComponent.class);
    Assert.assertEquals(component.init, 1, "'init' method must be called just once");
  }

  @Test
  public void testDestroy() {
    TestComponent component = injector.getInstance(TestComponent.class);
    injector.getInstance(Destroyer.class).destroy();
    Assert.assertEquals(component.destroy, 1, "'destroy' method must be called just once");
  }

  public static class MyModule implements Module {
    @Override
    public void configure(Binder binder) {
      binder.bind(TestComponent.class);
    }
  }

  public abstract static class SuperClass {
    int init;
    int destroy;

    @PostConstruct
    public void init() {
      init++;
    }

    @PreDestroy
    public void destroy() {
      destroy++;
    }
  }

  @Singleton
  public static class TestComponent extends SuperClass {
    @Inject
    public TestComponent() {}

    @PostConstruct
    @Override
    public void init() {
      super.init();
    }

    @PreDestroy
    @Override
    public void destroy() {
      super.destroy();
    }
  }
}
