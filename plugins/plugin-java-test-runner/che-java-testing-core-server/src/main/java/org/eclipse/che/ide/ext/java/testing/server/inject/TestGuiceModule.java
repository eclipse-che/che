package org.eclipse.che.ide.ext.java.testing.server.inject;


import com.google.inject.AbstractModule;
import org.eclipse.che.ide.ext.java.testing.server.FrameworkFactory;
import org.eclipse.che.ide.ext.java.testing.server.TestRunner;
import org.eclipse.che.ide.ext.java.testing.server.TestRunner2;
import org.eclipse.che.ide.ext.java.testing.server.TestingService;
import org.eclipse.che.inject.DynaModule;

import static com.google.inject.multibindings.Multibinder.newSetBinder;


@DynaModule
public class TestGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        System.out.println("TestGuiceModule binder");
        newSetBinder(binder(), TestRunner2.class);
        bind(TestingService.class);
    }
}
