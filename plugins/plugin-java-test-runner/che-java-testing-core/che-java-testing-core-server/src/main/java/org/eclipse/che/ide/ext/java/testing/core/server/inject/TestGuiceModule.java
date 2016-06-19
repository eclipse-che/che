package org.eclipse.che.ide.ext.java.testing.core.server.inject;


import com.google.inject.AbstractModule;

import org.eclipse.che.ide.ext.java.testing.core.server.TestingService;
import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathProvider;
import org.eclipse.che.ide.ext.java.testing.core.server.framework.TestRunner;
import org.eclipse.che.inject.DynaModule;

import static com.google.inject.multibindings.Multibinder.newSetBinder;


@DynaModule
public class TestGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        System.out.println("TestGuiceModule binder");
        newSetBinder(binder(), TestRunner.class);
        newSetBinder(binder(), TestClasspathProvider.class);
        bind(TestingService.class);
    }
}
