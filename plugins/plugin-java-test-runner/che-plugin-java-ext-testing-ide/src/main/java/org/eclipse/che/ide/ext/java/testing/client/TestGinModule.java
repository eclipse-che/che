package org.eclipse.che.ide.ext.java.testing.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.java.testing.client.view.navigation.factory.TestResultNodeFactory;

@ExtensionGinModule
public class TestGinModule extends AbstractGinModule{
    @Override
    protected void configure() {
        install(new GinFactoryModuleBuilder().build(TestResultNodeFactory.class));
    }
}
