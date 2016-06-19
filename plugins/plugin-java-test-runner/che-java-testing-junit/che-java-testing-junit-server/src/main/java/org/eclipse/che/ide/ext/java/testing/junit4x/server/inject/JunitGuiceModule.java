package org.eclipse.che.ide.ext.java.testing.junit4x.server.inject;

import com.google.inject.AbstractModule;
import org.eclipse.che.ide.ext.java.testing.core.server.framework.TestRunner;
import org.eclipse.che.ide.ext.java.testing.junit4x.server.JUnit4TestRunner;
import org.eclipse.che.inject.DynaModule;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

@DynaModule
public class JunitGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        System.out.println("JunitGuiceModule binder");
        newSetBinder(binder(), TestRunner.class).addBinding().to(JUnit4TestRunner.class);
    }
}
