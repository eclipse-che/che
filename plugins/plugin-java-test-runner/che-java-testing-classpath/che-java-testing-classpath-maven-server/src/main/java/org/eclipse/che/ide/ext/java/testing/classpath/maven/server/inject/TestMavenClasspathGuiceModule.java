package org.eclipse.che.ide.ext.java.testing.classpath.maven.server.inject;

import com.google.inject.AbstractModule;
import org.eclipse.che.ide.ext.java.testing.classpath.maven.server.MavenTestClasspathProvider;
import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathProvider;
import org.eclipse.che.inject.DynaModule;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

@DynaModule
public class TestMavenClasspathGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        System.out.println("TestMavenClasspathGuiceModule binder");
        newSetBinder(binder(), TestClasspathProvider.class).addBinding().to(MavenTestClasspathProvider.class);
    }
}
