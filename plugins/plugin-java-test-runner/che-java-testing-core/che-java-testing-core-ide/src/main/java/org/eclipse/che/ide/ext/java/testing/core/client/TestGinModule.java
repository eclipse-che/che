package org.eclipse.che.ide.ext.java.testing.core.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.ext.java.testing.core.client.view.navigation.factory.TestResultNodeFactory;
import org.eclipse.che.ide.util.loging.Log;

@ExtensionGinModule
public class TestGinModule extends AbstractGinModule{
    @Override
    protected void configure() {
        Log.info(TestGinModule.class,"TestGinModule init");
        install(new GinFactoryModuleBuilder().build(TestResultNodeFactory.class));
//        GinMultibinder.newSetBinder(binder(), TestAction.class).addBinding().to(PlainJavaProjectWizardRegistrar.class);
        GinMultibinder.newSetBinder(binder(), TestAction.class);

    }
}
