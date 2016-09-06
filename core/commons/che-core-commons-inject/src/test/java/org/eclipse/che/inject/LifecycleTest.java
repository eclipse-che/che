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
package org.eclipse.che.inject;

import org.eclipse.che.inject.lifecycle.DestroyErrorHandler;
import org.eclipse.che.inject.lifecycle.DestroyModule;
import org.eclipse.che.inject.lifecycle.Destroyer;
import org.eclipse.che.inject.lifecycle.InitModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/** @author andrew00x */
public class LifecycleTest {
    Injector injector;

    @BeforeTest
    public void init() {
        injector = Guice.createInjector(new InitModule(PostConstruct.class),
                                        new DestroyModule(PreDestroy.class, DestroyErrorHandler.DUMMY),
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

    public static abstract class SuperClass {
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
        public TestComponent() {
        }

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
