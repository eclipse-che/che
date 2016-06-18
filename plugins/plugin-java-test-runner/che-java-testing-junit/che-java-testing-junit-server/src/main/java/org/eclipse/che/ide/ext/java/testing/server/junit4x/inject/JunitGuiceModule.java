package org.eclipse.che.ide.ext.java.testing.server.junit4x.inject;

import com.google.inject.AbstractModule;
import org.eclipse.che.ide.ext.java.testing.server.TestRunner2;
import org.eclipse.che.ide.ext.java.testing.server.junit4x.JUnit4TestRunner;
import org.eclipse.che.inject.DynaModule;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

@DynaModule
public class JunitGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        System.out.println("JunitGuiceModule binder");
        newSetBinder(binder(), TestRunner2.class).addBinding().to(JUnit4TestRunner.class);
    }
}
