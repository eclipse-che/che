package org.eclipse.che.ide.ext.java.testing.junit4x.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.java.testing.core.client.TestAction;
import org.eclipse.che.ide.ext.java.testing.junit4x.client.view.navigation.factory.TestResultNodeFactory;
import org.eclipse.che.ide.util.loging.Log;

@ExtensionGinModule
public class JUnitGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        Log.info(JUnitGinModule.class,"JUnitGinModule init");
        install(new GinFactoryModuleBuilder().build(TestResultNodeFactory.class));
        GinMultibinder.newSetBinder(binder(), TestAction.class).addBinding().to(JUnitTestAction.class);

    }
}
